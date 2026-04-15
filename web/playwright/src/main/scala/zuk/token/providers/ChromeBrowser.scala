package zuk.token.providers

import com.microsoft.playwright.{Browser, BrowserContext, Playwright, Route}
import zuk.token.providers.deepseek.DeepseekWebAuth

import java.net.http.HttpClient
import java.time.Duration
import java.util
import java.util.List
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object ChromeBrowser {

  val httpClient = HttpClient.newBuilder.connectTimeout(Duration.ofSeconds(15)).build

  private val playwright: Playwright = Playwright.create

  private val browser: Browser = playwright.chromium.connectOverCDP("http://127.0.0.1:9222")

  private val browserContexts: util.List[BrowserContext] = browser.contexts

  val browserContext: BrowserContext = if(browserContexts!=null && browserContexts.size()>0){
    browserContexts.asScala.head
  }
  else {
    null
  }

  ChromeBrowser.onListen() //添加监听

  val chatList = Array(new DeepseekWebAuth()).toBuffer

  private def onListen(): Unit = {
    if (browserContext != null){

      //输出cookies
//      val cookies = browserContext.cookies(chatList.map(_.llmChatURL()).toList.asJava).asScala
//      val cookies = browserContext.cookies("https://chat.deepseek.com").asScala

      browserContext.route("**/*", (route: Route) => {
        try {
          val request = route.request()
          val url = request.url()
          println(s"url:${url}")

          chatList.foreach(chat=>{
            chat.onListen(route)
          })
        }
        catch {
          case exception: Exception => exception.printStackTrace()
        }
      })
    }
  }

}
