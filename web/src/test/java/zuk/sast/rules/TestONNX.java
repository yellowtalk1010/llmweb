package zuk.sast.rules;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import ai.onnxruntime.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.LongBuffer;
import java.util.*;

public class TestONNX {

    /***
     * 字典
     */
    static class Vocab {
        Map<String, Integer> token2id;

        public Vocab(String path) throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            Map<?,?> obj = mapper.readValue(new File(path), Map.class);
            String formatJson = JSONObject.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
            //System.out.println(formatJson);
            token2id = (Map<String,Integer>) obj.get("token2id");
        }

        public int getId(String tok) {
            return token2id.getOrDefault(tok, 1); // 1 = <UNK>
        }
    }

    static List<String> tokenize(String text, JiebaSegmenter segmenter) {
        List<String> toks = new ArrayList<>();
        List<SegToken> segs = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
        for (SegToken s : segs) {
            toks.add(s.word);
        }
        return toks;
    }

    static class Encoded {
        long[] ids;
        long[] mask;
        List<String> toks;
    }

    static Encoded encode(String text, Vocab vocab, int maxLen, JiebaSegmenter segmenter) {
        List<String> toks = tokenize(text, segmenter);
        List<Long> ids = new ArrayList<>();
        List<Long> mask = new ArrayList<>();
        for (String tok : toks) {
            ids.add((long)vocab.getId(tok));
            mask.add(1L);
        }
        if (ids.size() > maxLen) {
            ids = ids.subList(0, maxLen);
            mask = mask.subList(0, maxLen);
        }
        while (ids.size() < maxLen) {
            ids.add(0L);
            mask.add(0L);
        }
        Encoded e = new Encoded();
        e.ids = ids.stream().mapToLong(l -> l).toArray();
        e.mask = mask.stream().mapToLong(l -> l).toArray();
        e.toks = toks;
        return e;
    }

    static float[] softmax(float[] x) {
        double max = Double.NEGATIVE_INFINITY;
        for (float v : x) if (v > max) max = v;
        double sum = 0.0;
        double[] exps = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            exps[i] = Math.exp(x[i] - max);
            sum += exps[i];
        }
        float[] out = new float[x.length];
        for (int i = 0; i < x.length; i++) out[i] = (float)(exps[i]/sum);
        return out;
    }

    static int argmax(float[] x) {
        int idx = 0;
        for (int i = 1; i < x.length; i++) if (x[i] > x[idx]) idx = i;
        return idx;
    }

    public static void main(String[] args) throws Exception {
        String path = "src\\test\\resources\\modules\\module1\\";
        JiebaSegmenter segmenter = new JiebaSegmenter();
        Vocab vocab = new Vocab(path + "vocab.json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> labels = mapper.readValue(new File(path + "labels.json"), Map.class);

        String[] texts = {
                "客服态度特别好，下次还来",
                "一般般",
                "太糟糕了，完全不推荐"
        };
        int maxLen = 32;

        try (OrtEnvironment env = OrtEnvironment.getEnvironment();
             OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
             OrtSession session = env.createSession(path + "sentiment.onnx", opts)) {

            for (String text : texts) {
                Encoded e = encode(text, vocab, maxLen, segmenter);
                long unkCount = Arrays.stream(e.ids).filter(x -> x==1).count();
                double unkRatio = (double)unkCount / e.toks.size();
                System.out.println("----");
                System.out.println("TEXT: " + text);
                System.out.println("TOKENS: " + e.toks);
                System.out.println("IDS: " + Arrays.toString(e.ids));
                System.out.println("ATTN_MASK: " + Arrays.toString(e.mask));
                System.out.println("UNK_RATIO: " + unkRatio);

                OnnxTensor idsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(e.ids), new long[]{1,maxLen});
                OnnxTensor maskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(e.mask), new long[]{1,maxLen});

                Map<String,OnnxTensor> inputs = new HashMap<>();
                inputs.put("input_ids", idsTensor);
                inputs.put("attention_mask", maskTensor);

                try (OrtSession.Result result = session.run(inputs)) {
                    float[][] logits = (float[][]) result.get(0).getValue();
                    float[] probs = softmax(logits[0]);
                    int pred = argmax(probs);

                    StringBuilder sb = new StringBuilder("[");
                    for (int i=0;i<probs.length;i++){
                        if(i>0) sb.append(", ");
                        sb.append(String.format("%.4f", probs[i]));
                    }
                    sb.append("]");

                    System.out.printf("PRED: %s | probs=%s%n", labels.get(String.valueOf(pred)), sb.toString());
                }
            }
        }
    }
}
