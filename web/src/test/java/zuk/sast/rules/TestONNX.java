package zuk.sast.rules;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import ai.onnxruntime.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.labs.mlrg.olcut.util.Pair;
import org.tribuo.*;
import org.tribuo.provenance.ModelProvenance;
import org.tribuo.provenance.SimpleDataSourceProvenance;

import java.io.File;
import java.nio.LongBuffer;
import java.nio.file.Paths;
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

//    private static void showDataMeta(String onnxModelPath){
//        // 1. 创建ONNX环境
//        OrtEnvironment env = OrtEnvironment.getEnvironment();
//        // 2. 指定模型路径
//        String modelPath = onnxModelPath;
//
//        // 3. 使用Tribuo加载ONNX模型
//        // 注意：我们需要一个OutputFactory和假输入来定义输出类型。
//        // 对于纯 inspection（检查），输出类型可能不重要，但加载需要它。
//        OutputFactory<?> outputFactory = null; // 通常你需要一个具体的工厂，例如LabelFactory
//        // 这里我们创建一个ONNXExternalModel，它继承自Model
//        ONNXExternalModel<?> model = ONNXExternalModel.createONNXModel(
//                Paths.get(modelPath),
//                "my-model", // 模型名称
//                outputFactory, // 可能需要根据你的任务提供一个，例如用于分类的LabelFactory
//                new SimpleDataSourceProvenance("", outputFactory) // 数据源证明
//        );
//
//        // 4. 提取模型基本信息 (通过Tribuo的Model接口)
//        System.out.println("=== 模型基本信息 (Tribuo) ===");
//        System.out.println("模型名称: " + model.getName());
//        System.out.println("输入特征数: " + model.getNumberOfFeatures());
//        System.out.println("特征名称: " + model.getFeatureIDMap().getKeySet());
//
//        // 5. 获取底层ONNX Session以进行深入分析 (关键步骤)
//        OrtSession session = model.getSession();
//        System.out.println("\n=== ONNX 模型详细信息 ===");
//
//        // 5.1 打印输入信息
//        System.out.println("\n--- 模型输入 ---");
//        Map<String, NodeInfo> inputs = session.getInputInfo();
//        for (Map.Entry<String, NodeInfo> input : inputs.entrySet()) {
//            String name = input.getKey();
//            NodeInfo info = input.getValue();
//            System.out.println("输入名称: " + name);
//            // 尝试获取TensorInfo以查看形状和类型
//            if (info.getInfo() instanceof TensorInfo) {
//                TensorInfo tensorInfo = (TensorInfo) info.getInfo();
//                System.out.println("  形状: " + java.util.Arrays.toString(tensorInfo.getShape()));
//                System.out.println("  数据类型: " + tensorInfo.type);
//            }
//        }
//
//        // 5.2 打印输出信息
//        System.out.println("\n--- 模型输出 ---");
//        Map<String, NodeInfo> outputs = session.getOutputInfo();
//        for (Map.Entry<String, NodeInfo> output : outputs.entrySet()) {
//            String name = output.getKey();
//            NodeInfo info = output.getValue();
//            System.out.println("输出名称: " + name);
//            if (info.getInfo() instanceof TensorInfo) {
//                TensorInfo tensorInfo = (TensorInfo) info.getInfo();
//                System.out.println("  形状: " + java.util.Arrays.toString(tensorInfo.getShape()));
//                System.out.println("  数据类型: " + tensorInfo.type);
//            }
//        }
//
//        // 5.3 (高级) 获取计算图并遍历节点/算子
//        // 注意：直接访问图可能需要更底层的ONNX Runtime API调用。
//        // session.getSession() 返回的 OrtSession 允许你获取元数据，
//        // 但要遍历所有节点，通常需要处理ONNX的GraphProto。
//        // 一个更简单的方法是使用 session.getInputNames() 和 session.getOutputNames()，
//        // 但完整遍历所有算子需要更多工作，可能涉及 model.getSession().getSessionOptions()...
//
//        System.out.println("\n操作已完成。");
//
//        // 6. 关闭资源
//        session.close();
//        env.close();
//    }

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

        try {

            OrtEnvironment env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            OrtSession session = env.createSession(path + "sentiment.onnx", opts);

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
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
