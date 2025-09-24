package zuk.sast.controller.utils

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration

object HttpClientUtil {

  /**
   * 发送GET请求
   *
   * @param url 请求的URL
   * @return 响应内容
   * @throws Exception 如果请求失败
   */
  def sendGetRequest(url: String): String = {

    // 创建HttpClient
    val client = HttpClient.newBuilder
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(10))
      .followRedirects(HttpClient.Redirect.ALWAYS)
      .build

    // 创建HttpRequest
    val request = HttpRequest.newBuilder
      .uri(URI.create(url))
      .header("Accept", "application/json")
      .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
      .GET
      .build

    // 发送请求并获取响应
    val response = client.send(
      request,
      HttpResponse.BodyHandlers.ofString
    )

    // 检查响应状态码
    if (response.statusCode != 200) {
      throw new RuntimeException("HTTP错误: " + response.statusCode)
    }

    response.body
  }
}
