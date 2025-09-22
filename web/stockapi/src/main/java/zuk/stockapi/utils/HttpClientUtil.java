package zuk.stockapi.utils;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpClientUtil {

    /**
     * 发送GET请求
     * @param url 请求的URL
     * @return 响应内容
     * @throws Exception 如果请求失败
     */
    public static String sendGetRequest(String url) throws Exception {
        // 创建HttpClient
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // 创建HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
//                .header("host", "www.stockapi.com.cn")
                .GET()
                .build();

        // 发送请求并获取响应
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        // 检查响应状态码
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP错误: " + response.statusCode());
        }

        return response.body();
    }
}
