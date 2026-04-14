package zuk.token.providers.deepseek

import com.alibaba.fastjson2.JSONObject
import com.microsoft.playwright.{BrowserContext, Locator, Page, Response, Route}
import zuk.token.providers.{ChromeBrowser, IToken}

import java.util
import java.util.Map
import scala.jdk.CollectionConverters.*

class DeepseekWebAuth extends IToken {

  /***
   * deepseek官网地址
   */
  val deepseekWebChatURL: String = "https://chat.deepseek.com"

  /***
   * deepseek聊天界面
   */
  var deepseekPage: Page = null

  var isBreak: Boolean = false

  private def installIntercept(context: BrowserContext, content: String): Unit = {
    context.route("**/*", (route: Route) => {
      try {
        val request = route.request()
        val url = request.url()

        if (url.contains("/api/v0/") && url.contains("completion")) {
          val headers = request.headers()
          val authorization = headers.get("authorization")
          val postData = request.postData() // 可能为 null

          println(s"[intercept] url = $url")
          println(s"[intercept] authorization = $authorization")
          println(s"[intercept] postData = $postData")

          if (postData != null && postData.contains("chat_session_id")) {
            // 在这里解析参数
            // val jsonObj = JSONObject.parseObject(postData)

            // 直接取消，不让它真正发出去
            println("[intercept] matched, abort request")
            route.abort()

            val updatePostData = postData.replaceAll("hi", content)
            val deepseekClient = new DeepseekClient
            deepseekClient.createCompletion(headers, updatePostData)

          } else {
            // 不符合条件，放行
            route.resume() // 有些版本也常写 route.continue()
          }
        } else {
          route.resume()
        }
      } catch {
        case exception: Exception =>
          exception.printStackTrace()
          route.resume()
      }
    })
  }

  override def chat(content: String): Unit = {
    val browserContext = ChromeBrowser.browserContext
    if(browserContext == null){
      return
    }
    val exist = browserContext.pages().asScala.map(_.url()).toSet.filter(_.startsWith(deepseekWebChatURL)).size > 0
    if(exist){
      //如果存在，则输出全部url
      browserContext.pages().asScala.foreach(page => {
        val url = page.url()
        println(s"url地址:${url}")
        if(this.deepseekPage==null && page.url().startsWith(deepseekWebChatURL)){
          this.deepseekPage = page
        }
      })
    }
    else {
      //创建一个新页面
      this.deepseekPage = browserContext.newPage()
      //导航到登录页面
      this.deepseekPage.navigate(deepseekWebChatURL)
    }
//    val cookies = browserContext.cookies(deepseekWebChatURL).asScala
//    val cookieStr = cookies.map(c=>{
//      s"${c.name}=${c.value}"
//    }).mkString("; ")
//    println(s"Cookie:${cookieStr}")

//    val userAgent = this.deepseekPage.evaluate("() => navigator.userAgent").asInstanceOf[String]
//    println("User-Agent: " + userAgent)

    installIntercept(browserContext, content)

    while (!isBreak){
      sayHi()
      Thread.sleep(2000)
    }

    println("over")
  }

  /***
   * 点击按钮发送：hi
   */
  override def sayHi(): Unit = {
    //向输入框中输入hi
    val textarea = this.deepseekPage.locator("textarea[placeholder='给 DeepSeek 发送消息 ']")
    textarea.waitFor()
    textarea.fill("hi")

    //点击发送按钮
    val sendButton = this.deepseekPage.locator("div[role='button'][aria-disabled='false']").last
    sendButton.click()
  }



  override def delete(): Unit = {}

  override def llmName(): String = "deepseek"
  override def llmChatURL(): String = "https://chat.deepseek.com"
  override def llmOfficialWebsite(): String = "https://www.deepseek.com/"
}
