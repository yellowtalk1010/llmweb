package zuk.token.providers

import com.microsoft.playwright.{Browser, BrowserContext, Playwright, Route}
import zuk.token.providers.deepseek.DeepseekWeb

import java.net.http.HttpClient
import java.time.Duration
import java.util
import java.util.concurrent
import java.util.concurrent.Executors
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

  private val chatList = Array[IProviderToken]().toBuffer


  val executors = concurrent.Executors.newCachedThreadPool()

  def init(list: List[IProviderToken]): Unit = {

    chatList ++= list

    //启动监听程序
    println("启动监听")
    if (browserContext != null){
      this.onListenRequest()
      chatList.foreach(e=>{
        executors.execute(e)
      })
    }
  }


  private def onListenRequest(): Unit = {
    try {

      println("输出全部URL地址")
      val urls = browserContext.pages().asScala.map(_.url()).toList
      urls.foreach(println)

      println("输出全部cookie")
      val cookies = browserContext.cookies(urls.asJava)
      cookies.asScala.foreach(c => {
        val k = c.name
        val v = c.value
        println(s"${k}=${v}")
      })

      browserContext.onRequest(handle=>{
        val url = handle.url()
        chatList.foreach(c=>{
          println(s"启动${c.getClass.getSimpleName}模型监听")
          c.onListenRequest(handle)
        })
      })
    }
    catch {
      case exception: Exception =>
    }
  }

}
