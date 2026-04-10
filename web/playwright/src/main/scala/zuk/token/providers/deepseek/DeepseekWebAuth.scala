package zuk.token.providers.deepseek

import com.alibaba.fastjson2.JSONObject
import com.microsoft.playwright.{BrowserContext, Locator, Page, Response, Route}
import zuk.token.providers.ChromeBrowser

import java.util
import java.util.Map
import scala.jdk.CollectionConverters.*

class DeepseekWebAuth {

  val deepseekWebChatURL: String = "https://chat.deepseek.com"
  var isBreak: Boolean = false

  def installIntercept(context: BrowserContext): Unit = {
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

            val updatePostData = postData.replaceAll("hi", "用joern实现c/c++/joern的数据溢出问题")
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

    installIntercept(browserContext)

    // TODO 如何监听
    deepseekPage.onRequest(request=>{
      try {
        val url = request.url
        println(s"onRequest.url:${url}")
        if (url.contains("/api/v0/") && url.contains("completion")) {
          val headers = request.headers
          val authorization = headers.get("authorization")
          val postData = request.postData()
          println(authorization)
          if(postData.contains("chat_session_id")){
            isBreak = true

            println(s"postData:${postData}")

            val jsonObj = JSONObject.parseObject(postData)
            println()

            val response = request.response()
            if(response!=null && response.ok()){
              val responseText = response.text()
              println(s"responseText:${responseText}")

              Thread.sleep(2000)

//              val deepseekClient = new DeepseekClient
//              deepseekClient.createPowChallenge()
//              deepseekClient.createCompletion()
            }

          }
        }
      } catch {
        case e: Exception => e.printStackTrace()
      }
    })

    while (!isBreak){
      sayHi(deepseekPage)
      Thread.sleep(2000)
    }

    println("over")
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
