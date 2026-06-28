package zuk.token.providers

import com.microsoft.playwright.{Browser, BrowserContext, Playwright, Route}
import zuk.token.providers.deepseek.DeepseekWeb

import java.net.http.HttpClient
import java.time.Duration
import java.util
import java.util.{List, concurrent}
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

  val chatList = Array[IProviderToken]().toBuffer

  def init(): Unit = {
    //添加deepseek人工智能
    chatList += new DeepseekWeb()

    //启动监听程序
    onListen()

    //执行ai分析
//    val executors = concurrent.Executors.newCachedThreadPool()
//    chatList.foreach(e => {
//      println("启动任务处理线程池")
//      executors.execute(e)
//    })
  }

  private def onListen(): Unit = {
    println("启动监听")
    if (browserContext != null){
      this.onListenRequest()
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
