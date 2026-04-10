package zuk.token.providers.deepseek

import zuk.sast.DeepseekWebAuth

import java.io.IOException
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util
import java.util.{LinkedHashMap, Map}
import scala.jdk.CollectionConverters.*

class DeepseekClient {

  private val httpClient = HttpClient.newBuilder.connectTimeout(Duration.ofSeconds(15)).build

  @throws[IOException]
  @throws[InterruptedException]
  def createPowChallenge(): Unit = {
    val targetPath = "/api/v0/chat/completion"

    System.out.println("[DeepSeekWebClient] Creating PoW challenge for " + targetPath + "...")
    val body = new util.LinkedHashMap[String, AnyRef]
    body.put("target_path", targetPath)
    val request = requestBuilder("https://chat.deepseek.com/api/v0/chat/create_pow_challenge", "POST", DeepseekWebAuth.OBJECT_MAPPER.writeValueAsString(body), fetchHeaders, null).build
    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString)

    println(s"response.statusCode:${response.statusCode()}")
    println(s"response.body:${response.body()}")

//    if (response.statusCode / 100 != 2) throw new IOException("Failed to create PoW challenge: " + response.statusCode + " " + response.body)
//    val data = DeepseekWebAuth.OBJECT_MAPPER.readValue(response.body, classOf[DeepseekWebClient.DeepSeekPowResponse])
//    var challenge: DeepseekWebClient.DeepSeekPowChallenge = null
//    if (data != null && data.data != null && data.data.bizData != null && data.data.bizData.challenge != null) challenge = data.data.bizData.challenge
//    else if (data != null && data.data != null && data.data.challenge != null) challenge = data.data.challenge
//    else if (data != null && data.challenge != null) challenge = data.challenge
//    if (challenge == null) throw new IOException("PoW challenge missing in response")
//    challenge
  }

  def fetchHeaders: util.Map[String, String] = {

    val bearer = "ah0leAC28XQ+vIjpAMvVGohher7h6UHWNcHGLJ18eYRS2TF6OB4fAFZNQJbs8lTs"
    val cookie = "smidV2=2026040214172066923d790d84a4e40e4da44b1928e70e00966c73ccbd89eb0; .thumbcache_6b2e5483f9d858d7c661c5e276b6a6ae=l8NH07eWhDqnSZjq6rlTryPW0Kbb/XdHUqSBSj2zfseWYjIBnzoOyYRTc4S0vNKhi3pofHKMBz8ZEFhbkKzU/A%3D%3D; HWWAFSESTIME=1775704333193; HWWAFSESID=b0e47ad2ff33c196336; ds_session_id=3fb55ca55b504c08aa8a70672378d762"
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36"
    val BASE_URL = "https://chat.deepseek.com"

    val headers = new util.LinkedHashMap[String, String]
    headers.put("Cookie", cookie)
    headers.put("User-Agent", userAgent)
    headers.put("Content-Type", "application/json")
    headers.put("Accept", "*/*")
    if (!bearer.isBlank) headers.put("Authorization", "Bearer " + bearer)
    headers.put("Referer", BASE_URL + "/")
    headers.put("Origin", BASE_URL)
    headers.put("x-client-platform", "web")
    headers.put("x-client-version", "1.7.0")
    headers.put("x-app-version", "20241129.1")
    headers.put("x-client-locale", "zh_CN")
    headers.put("x-client-timezone-offset", "28800")

    headers
  }


  private def requestBuilder(url: String,
                             method: String,
                             body: String,
                             headers: util.Map[String, String],
                             timeout: Duration) = {
    val builder = HttpRequest.newBuilder.uri(URI.create(url))
    if (timeout != null) {
      builder.timeout(timeout)
    }
    if ("POST".equalsIgnoreCase(method)) {
      builder.POST(HttpRequest.BodyPublishers.ofString(if (body == null) {
        ""
      }
      else {
        body
      }, StandardCharsets.UTF_8))
    }
    else {
      builder.GET
    }

    for (entry <- headers.entrySet().asScala) {
      if (entry.getValue != null && !entry.getValue.isBlank) {
        builder.header(entry.getKey, entry.getValue)
      }
    }

    builder
  }

}
