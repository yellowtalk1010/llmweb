package vision.sast.rules;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessTest {

    private static ExecutorService pool = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws Exception {
        String continueCorePath = "D:\\development\\github\\continue\\extensions\\intellij\\build\\idea-sandbox\\plugins-uiTest\\continue-intellij-extension\\core\\win32-x64\\continue-binary.exe"; // 替换为你的实际路径

        File file = new File(continueCorePath);
        File parentDir = file.getParentFile();

        System.out.println(continueCorePath);
//        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", continueCorePath);
        ProcessBuilder processBuilder = new ProcessBuilder(continueCorePath);
        processBuilder.directory(parentDir);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

//        Process process = Runtime.getRuntime().exec(continueCorePath);

        // 可以处理 process 的输入输出流等

        OutputStream outputStream = process.getOutputStream();
        InputStream inputStream = process.getInputStream();

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//        process.onExit().thenRun(()->{
//            try {
//                writer.close();
//                reader.close();
//                process.destroy();
//            }catch (Exception exception) {
//                exception.printStackTrace();
//            }
//        });


        pool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        System.out.println("监听");
                        String line = reader.readLine();
                        if (line == null && StringUtils.isNotEmpty(line)) {
                            System.out.println("接受到的数据是；" + line);
                        }
                        else {
                            Thread.sleep(100);
                        }
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });



        String sss = "writer>>>h<<<:";
        Arrays.stream(getString().split("\n")).filter(line->{
          return  line.trim().startsWith(sss);
        }).map(line->{
            String subStr = line.trim().substring(sss.length());
            return subStr;
        }).forEach(line->{
            try {
                System.out.println("输入：" + line);
                writer.write(line+ "\r\n");
                writer.flush();
                Thread.sleep(2000);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        System.out.println("finished.");

    }

    static String getString(){
        String str = """
                writer>>>h<<<:{"messageId":"49f115a7-bf5d-4a2d-b49e-339a02911406","messageType":"getIdeInfo","data":{"ideType":"jetbrains","name":"IntelliJ IDEA 2022.3.3","version":"2022.3.3","remoteName":"local","extensionVersion":"1.0.8"}}
                writer>>>h<<<:{"messageId":"b340defc-e886-4407-86a8-b342a8c5ff7c","messageType":"getIdeSettings","data":{"remoteConfigSyncPeriod":60,"userToken":"","enableControlServerBeta":false,"pauseCodebaseIndexOnStart":false,"continueTestEnvironment":"production"}}
                writer>>>h<<<:{"messageId":"67b21eb2-b1c9-457f-a8d5-8312b762109e","messageType":"getControlPlaneSessionInfo"}
                read:{"messageType":"getIdeInfo","messageId":"156ac3cf-ba6c-45ec-ba96-043b695e2b66"}
                writer>>>h<<<:{"messageId":"156ac3cf-ba6c-45ec-ba96-043b695e2b66","messageType":"getIdeInfo","data":{"ideType":"jetbrains","name":"IntelliJ IDEA 2022.3.3","version":"2022.3.3","remoteName":"local","extensionVersion":"1.0.8"}}
                read:{"messageType":"files/opened","data":{"done":true,"status":"success"},"messageId":"73901e21-0f54-4395-82f2-920070520278"}
                read:{"messageType":"getWorkspaceDirs","messageId":"d2b2b43e-f707-4a8b-a668-e63970ab3d58"}
                read:{"messageType":"config/getSerializedProfileInfo","data":{"done":true,"content":{"result":{"errors":[],"configLoadInterrupted":true},"profileId":null},"status":"success"},"messageId":"13f9042a-acfc-4abe-8e08-cfc6e244976b"}
                Error handling message: {"messageType":"config/getSerializedProfileInfo","data":{"done":true,"content":{"result":{"errors":[],"configLoadInterrupted":true},"profileId":null},"status":"success"},"messageId":"13f9042a-acfc-4abe-8e08-cfc6e244976b"}
                java.lang.NullPointerException: null cannot be cast to non-null type kotlin.collections.Map<kotlin.String, kotlin.Any>
                read:{"messageType":"getWorkspaceDirs","messageId":"87b23a8e-e52f-443a-89f8-7ea0421e20fb"}
                writer>>>h<<<:{"messageId":"d2b2b43e-f707-4a8b-a668-e63970ab3d58","messageType":"getWorkspaceDirs","data":["file:///D:/hello.txt"]}
                writer>>>h<<<:{"messageId":"87b23a8e-e52f-443a-89f8-7ea0421e20fb","messageType":"getWorkspaceDirs","data":["file:///D:/hello.txt"]}
                read:{"messageType":"indexProgress","data":{"progress":0,"desc":"Starting indexing","status":"loading"},"messageId":"9b4e6021-ecdf-4314-8e0f-7578cb417aad"}
                read:{"messageType":"getRepoName","data":{"dir":"file:///D:/hello.txt"},"messageId":"b82d5c62-3aa9-494c-89ba-5c9a44039a49"}
                read:{"messageType":"getWorkspaceDirs","messageId":"33f54430-d932-4ad9-a020-d66229898703"}
                writer>>>h<<<:{"messageId":"33f54430-d932-4ad9-a020-d66229898703","messageType":"getWorkspaceDirs","data":["file:///D:/hello.txt"]}
                read:{"messageType":"fileExists","data":{"filepath":"file:///D:/hello.txt/.continue/assistants"},"messageId":"9b6c17a1-9c43-429e-b184-cf2c19ead19c"}
                read:{"messageType":"fileExists","data":{"filepath":"file:///D:/development/github/continue/extensions/intellij/src/test/kotlin/com/github/continuedev/continueintellijextension/e2e/test-continue/assistants"},"messageId":"428fb315-f120-4969-8bb2-b999dad411f4"}
                writer>>>h<<<:{"messageId":"428fb315-f120-4969-8bb2-b999dad411f4","messageType":"fileExists","data":false}
                writer>>>h<<<:{"messageId":"9b6c17a1-9c43-429e-b184-cf2c19ead19c","messageType":"fileExists","data":false}
                read:{"messageType":"getWorkspaceDirs","messageId":"f12b48c4-056f-44c3-bf84-2fde6f143738"}
                writer>>>h<<<:{"messageId":"f12b48c4-056f-44c3-bf84-2fde6f143738","messageType":"getWorkspaceDirs","data":["file:///D:/hello.txt"]}
                read:{"messageType":"didChangeAvailableProfiles","data":{"profiles":[{"id":"local","profileType":"local","fullSlug":{"ownerSlug":"","packageSlug":"","versionSlug":""},"iconUrl":"","title":"Local Assistant","uri":"file:///D:/development/github/continue/extensions/intellij/src/test/kotlin/com/github/continuedev/continueintellijextension/e2e/test-continue/config.json"}],"selectedProfileId":"local"},"messageId":"288b5fc6-8cc5-4ea8-91cf-ff3e246eb8b7"}
                read:{"messageType":"getIdeInfo","messageId":"3c374c11-7bed-4ee5-a04c-157d08b77d51"}
                writer>>>h<<<:{"messageId":"3c374c11-7bed-4ee5-a04c-157d08b77d51","messageType":"getIdeInfo","data":{"ideType":"jetbrains","name":"IntelliJ IDEA 2022.3.3","version":"2022.3.3","remoteName":"local","extensionVersion":"1.0.8"}}
                read:{"messageType":"getWorkspaceConfigs","messageId":"3f642c0b-3dc5-4ac9-bd0c-874e31ca5e7c"}
                writer>>>h<<<:{"messageId":"3f642c0b-3dc5-4ac9-bd0c-874e31ca5e7c","messageType":"getWorkspaceConfigs","data":[]}
                read:{"messageType":"getIdeInfo","messageId":"892e40ce-c9a4-4608-8259-119aadbddf89"}
                writer>>>h<<<:{"messageId":"892e40ce-c9a4-4608-8259-119aadbddf89","messageType":"getIdeInfo","data":{"ideType":"jetbrains","name":"IntelliJ IDEA 2022.3.3","version":"2022.3.3","remoteName":"local","extensionVersion":"1.0.8"}}
                read:{"messageType":"getUniqueId","messageId":"5fbd524d-68ac-4d4d-b141-65fb61a48e38"}
                writer>>>h<<<:{"messageId":"b82d5c62-3aa9-494c-89ba-5c9a44039a49","messageType":"getRepoName"}
                read:{"messageType":"indexProgress","data":{"progress":0,"desc":"Starting indexing...","status":"loading"},"messageId":"e43947bc-5da4-45a4-b104-a1ea5e662ea3"}
                read:{"messageType":"indexProgress","data":{"progress":0,"desc":"Discovering files in hello.txt...","status":"indexing"},"messageId":"5caa1ea6-55fa-429c-ace7-9f4e4a1c3f10"}
                read:{"messageType":"listDir","data":{"dir":"file:///D:/hello.txt"},"messageId":"57786954-5f89-4aa8-969c-3c7fb5956d14"}
                writer>>>h<<<:{"messageId":"57786954-5f89-4aa8-969c-3c7fb5956d14","messageType":"listDir","data":[]}
                read:{"messageType":"getBranch","data":{"dir":"file:///D:/hello.txt"},"messageId":"49f3845c-c2e7-4d6f-adf7-616ac8876663"}
                writer>>>h<<<:{"messageId":"49f3845c-c2e7-4d6f-adf7-616ac8876663","messageType":"getBranch","data":"NONE"}
                read:{"messageType":"getRepoName","data":{"dir":"file:///D:/hello.txt"},"messageId":"1262fe7e-3d85-41a9-94e9-db2790107596"}
                writer>>>h<<<:{"messageId":"5fbd524d-68ac-4d4d-b141-65fb61a48e38","messageType":"getUniqueId","data":"44-A3-BB-C3-6E-EF"}
                read:{"messageType":"getWorkspaceDirs","messageId":"05bfdc54-857d-4145-8fab-200f5495f1c4"}
                writer>>>h<<<:{"messageId":"05bfdc54-857d-4145-8fab-200f5495f1c4","messageType":"getWorkspaceDirs","data":["file:///D:/hello.txt"]}
                read:{"messageType":"fileExists","data":{"filepath":"file:///D:/hello.txt/.continuerules"},"messageId":"488b01d0-dd6b-456a-bf55-64d4519d5305"}
                writer>>>h<<<:{"messageId":"488b01d0-dd6b-456a-bf55-64d4519d5305","messageType":"fileExists","data":false}
                read:{"messageType":"getWorkspaceDirs","messageId":"d2f5a2fe-d83e-4c3b-b621-62d25e9246ff"}
                writer>>>h<<<:{"messageId":"d2f5a2fe-d83e-4c3b-b621-62d25e9246ff","messageType":"getWorkspaceDirs","data":["file:///D:/hello.txt"]}
                read:{"messageType":"fileExists","data":{"filepath":"file:///D:/hello.txt/.continue/prompts"},"messageId":"0cc09628-e72c-4a2f-9b70-e4cbe8c3547c"}
                read:{"messageType":"fileExists","data":{"filepath":"file:///D:/hello.txt/.prompts"},"messageId":"fecd9cee-0fd8-43a5-b193-0552fbf5762b"}
                writer>>>h<<<:{"messageId":"0cc09628-e72c-4a2f-9b70-e4cbe8c3547c","messageType":"fileExists","data":false}
                writer>>>h<<<:{"messageId":"fecd9cee-0fd8-43a5-b193-0552fbf5762b","messageType":"fileExists","data":false}
                read:{"messageType":"isTelemetryEnabled","messageId":"286e9d77-ce13-4951-9fba-ce5228f3c702"}
                writer>>>h<<<:{"messageId":"286e9d77-ce13-4951-9fba-ce5228f3c702","messageType":"isTelemetryEnabled","data":true}
                read:{"messageType":"getUniqueId","messageId":"9f25aab9-838d-40bf-947e-07b889d1726b"}
                writer>>>h<<<:{"messageId":"1262fe7e-3d85-41a9-94e9-db2790107596","messageType":"getRepoName"}
                read:{"messageType":"getFileStats","data":{"files":[]},"messageId":"97b7781d-c81d-44f4-ba78-0dad6cae386e"}
                writer>>>h<<<:{"messageId":"97b7781d-c81d-44f4-ba78-0dad6cae386e","messageType":"getFileStats","data":{}}
                writer>>>h<<<:{"messageId":"9f25aab9-838d-40bf-947e-07b889d1726b","messageType":"getUniqueId","data":"44-A3-BB-C3-6E-EF"}
                read:{"messageType":"getWorkspaceDirs","messageId":"b498ed9b-9023-4798-8d8f-e34f76eec16d"}
                read:{"messageType":"getIdeSettings","messageId":"2fef3ac3-7b77-4ba0-b636-471bf0cb7ea1"}
                writer>>>h<<<:{"messageId":"b498ed9b-9023-4798-8d8f-e34f76eec16d","messageType":"getWorkspaceDirs","data":["file:///D:/hello.txt"]}
                read:{"messageType":"indexProgress","data":{"progress":1,"desc":"Indexing Complete","status":"done"},"messageId":"7edb6515-b867-4d34-95f0-0d8fb230b6d8"}
               """;
        return str;
    }
}
