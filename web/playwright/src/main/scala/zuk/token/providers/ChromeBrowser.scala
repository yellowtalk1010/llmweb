package zuk.token.providers

import com.microsoft.playwright.{Browser, BrowserContext, Playwright, Route}
import zuk.token.providers.deepseek.DeepseekWeb

import java.net.http.HttpClient
import java.time.Duration
import java.util
import java.util.List
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object ChromeBrowser {

  val httpClient = HttpClient.newBuilder.connectTimeout(Duration.ofSeconds(15)).build

  private val playwright: Playwright = Playwright.create

  val browser: Browser = playwright.chromium.connectOverCDP("http://127.0.0.1:9222")

  private val browserContexts: util.List[BrowserContext] = browser.contexts

  val browserContext: BrowserContext = if(browserContexts!=null && browserContexts.size()>0){
    browserContexts.asScala.head
  }
  else {
    println("未监听chrome浏览器")
    null
  }

  ChromeBrowser.onListen() //添加监听

  val chatList = Array[IToken]().toBuffer

  private def onListen(): Unit = {
    if (browserContext != null){
      //this.onListenRoute()
      this.onListenRequest()
    }
  }


  private def onListenRequest(): Unit = {
    try {
      browserContext.onRequest(handle=>{
        val url = handle.url()
        chatList.foreach(c=>{
          c.onListenRequest(handle)
        })
      })
    }
    catch {
      case exception: Exception =>
    }
  }

  /***
   * 会出现阻塞情况
   */
  @Deprecated
  private def onListenRoute(): Unit = {

    browserContext.route("**/*", (route: Route) => {
      try {
        println("输出全部URL地址")
        val urls = browserContext.pages().asScala.map(_.url()).toList
        urls.foreach(println)
        println()

        println("输出全部cookie")
        val cookies = browserContext.cookies(urls.asJava)
        cookies.asScala.foreach(c => {
          val k = c.name
          val v = c.value
          println(s"${k}=${v}")
        })
        println()

        val request = route.request()
        val url = request.url()
        println(s"url:${url}")

        chatList.foreach(chat => {
          chat.onListenRoute(route)
        })
      }
      catch {
        case exception: Exception => exception.printStackTrace()
      }
    })
  }

}
