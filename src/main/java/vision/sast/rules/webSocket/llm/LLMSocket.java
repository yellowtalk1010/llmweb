package vision.sast.rules.webSocket.llm;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import vision.sast.rules.webSocket.LLMWebSocketHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LLMSocket {

    private static ExecutorService pool = Executors.newSingleThreadExecutor();
    public static PrintWriter writer = null;
    private static BufferedReader reader = null;

    /***
     * 初始化大模型连接器
     */
    public static synchronized void init(){
        try {
            if(writer != null || reader != null){
                return;
            }
            Socket socket = new Socket("127.0.0.1", 3000);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    if(writer!=null && reader!=null){
                        while (true) {
                            try {
                                String line = reader.readLine();
                                System.out.println("接收到：" + line);
                                if(line!=null && StringUtils.isNotEmpty(line)){
                                    try {
                                        LLMReponse llmReponse = JSONObject.parseObject(line, LLMReponse.class);
                                        if(llmReponse.getMessageType().equals("llm/streamChat") && !llmReponse.getData().done){

                                            String content = llmReponse.getData().getContent().getContent();
                                            System.out.print(content);

                                            if(llmReponse.getData().done){
                                                System.out.println("\n\n\n完成！\n\n");
                                            }
                                            else {
                                                //向websocket推荐llm返回的数据
                                                LLMWebSocketHandler.pushMessage(llmReponse);
                                            }

                                        }

                                    }catch (Exception e) {
                                        System.out.println();
                                        e.printStackTrace();
                                        break;
                                    }
                                }
                                else {
                                    Thread.sleep(100);
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
