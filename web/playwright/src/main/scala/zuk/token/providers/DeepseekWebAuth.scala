package zuk.token.providers

import com.microsoft.playwright.{Locator, Page, Response}

import java.util
import java.util.Map
import scala.jdk.CollectionConverters.*

class DeepseekWebAuth {

  val deepseekWebChatURL: String = "https://chat.deepseek.com"

  def webLogin(): Unit = {
    val browserContext = ChromeBrowser.browserContext
    if(browserContext == null){
      return
    }
    val exist = browserContext.pages().asScala.map(_.url()).toSet.filter(_.startsWith(deepseekWebChatURL)).size > 0
    var deepseekPage: Page = null
    if(exist){
      //如果存在，则输出全部url
      browserContext.pages().asScala.foreach(page => {
        val url = page.url()
        println(s"url地址:${url}")
        if(deepseekPage==null && page.url().startsWith(deepseekWebChatURL)){
          deepseekPage = page
        }
      })
    }
    else {
      //创建一个新页面
      deepseekPage = browserContext.newPage()
      //导航到登录页面
      deepseekPage.navigate(deepseekWebChatURL)
    }
    val cookies = browserContext.cookies(deepseekWebChatURL).asScala
    val cookieStr = cookies.map(c=>{
      s"${c.name}=${c.value}"
    }).mkString("; ")

    println(s"Cookie:${cookieStr}")

    val userAgent = deepseekPage.evaluate("() => navigator.userAgent").asInstanceOf[String]
    println("User-Agent: " + userAgent)

    deepseekPage.onRequest(request=>{
      try {
        val url = request.url
        println(s"onRequest.url:${url}")
        if (url.contains("/api/v0/") && url.contains("completion")) {
          val headers = request.headers
          val authorization = headers.get("authorization")
          val postData = request.postData()
          println(authorization)
        }
        else {
          //println(url)
        }
      } catch {
        case e: Exception => e.printStackTrace()
      }
    })
    Thread.sleep(1000)

    sayHi(deepseekPage)

  }

  /***
   * 点击按钮发送：hi
   */
  def sayHi(deepseekPage: Page): Unit = {
    val textarea = deepseekPage.locator("textarea[placeholder='给 DeepSeek 发送消息 ']")
    textarea.waitFor()
    textarea.fill("hi")
    val sendButton = deepseekPage.locator("div[role='button'][aria-disabled='false']").last
    sendButton.click()
  }

}
