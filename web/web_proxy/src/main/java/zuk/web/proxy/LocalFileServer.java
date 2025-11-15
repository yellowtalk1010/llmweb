package zuk.web.proxy;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/***
 * host + java的方式实现代理
 *
 * 在host中配置，abc.com是要访问的路径，127.0.0.1是重定向的本地服务地址
 *
 * C:\Windows\System32\drivers\etc\hosts
 *
 * 127.0.0.1 abc.com
 *
 *
 */
public class LocalFileServer {

    // 本地映射根目录，例如 C:\eee\
    private static final String LOCAL_BASE = "web_proxy/src/main/resources/";

    public static void main(String[] args) throws Exception {
        File dir = new File(LOCAL_BASE);
        System.out.println(dir.getAbsolutePath() + "，" + dir.exists());
        Arrays.stream(dir.listFiles()).forEach(System.out::println);
        int port = 80; // 必须和 hosts 保持一致

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Local file server running at http://localhost:" + port);

        server.createContext("/", new FileHandler());
        server.setExecutor(null);
        server.start();
    }

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            System.out.println("Request: " + requestPath);

            // 拼成本地文件路径，例如 /xxx.zip → C:/eee/xxx.zip
            String localPath = LOCAL_BASE + requestPath;

            File file = new File(localPath);

            if (!file.exists() || !file.isFile()) {
                String resp = "File not found: " + localPath;
                exchange.sendResponseHeaders(404, resp.length());
                OutputStream os = exchange.getResponseBody();
                os.write(resp.getBytes());
                os.close();
                return;
            }

            // 发送文件
            byte[] data = Files.readAllBytes(Paths.get(localPath));
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "application/octet-stream");

            exchange.sendResponseHeaders(200, data.length);
            OutputStream os = exchange.getResponseBody();
            os.write(data);
            os.close();
        }
    }
}
