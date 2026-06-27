package zuk.token.providers.deepseek

import com.microsoft.playwright.{Page, Request, Response, Route}
import zuk.token.providers.deepseek.tasks.{DeepseekTask_easymoneyConcept, ITask}
import zuk.token.providers.{ChromeBrowser, IToken}

import scala.jdk.CollectionConverters.*

class DeepseekWeb extends IToken {

  /***
   * deepseek官网地址
   */
  val deepseekWebChatURL: String = "https://chat.deepseek.com"

  /***
   * deepseek聊天界面
   */
  var deepseekPage: Page = null

  var isBreak: Boolean = false

  var chatContext: String = null

  override def onListenRequest(request: Request): Boolean = {
    try {
      val url = request.url()
      if(url.startsWith("https://chat.deepseek.com/api/v0/chat/completion")){
        println(s"监听onListenResponse.url:${url}")
        val response = request.response()
        val text = response.text()
        println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv")
        println(text)
        if(chatContext!=null){
          val task: ITask = new DeepseekTask_easymoneyConcept(chatContext)
          task.responseText = text
          val parseResult = task.parse()
          println(parseResult)
        }
        println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
      }
      true
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        false
    }
  }

  /***
   * 这个方式容易出现阻塞导致不问题
   */
  @Deprecated
  override def onListenRoute(route: Route): Boolean = {
    val request = route.request()
    val url = request.url()
    if(url.contains("/api/v0/") && url.contains("completion")){
      isBreak = true
      val headers = request.headers()
      val authorization = headers.get("authorization")
      val postData = request.postData() // 可能为 null

      println(s"[intercept] url = ${url}")
      println(s"[intercept] authorization = ${authorization}")
      println(s"[intercept] postData = ${postData}")

      if (postData != null && postData.contains("chat_session_id")) {
        // 在这里解析参数
        // val jsonObj = JSONObject.parseObject(postData)

        // 直接取消，不让它真正发出去
        println("[intercept] matched, abort request")
        route.abort()

        headers.asScala.map(h => {
          val k = h._1
          val v = h._2
          s"${k}=${v}"
        }).toList.foreach(println)

//        val updatePostData = postData.replaceAll("hi", content)
//        val deepseekClient = new DeepseekClient
//        deepseekClient.createCompletion(headers, updatePostData)

      }

      return true
    }
    else {
      route.resume()
    }
    false
  }

  override def chat(context: String): Unit = synchronized {

    chatContext = context
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

    println("开始对话")

    //向输入框中输入聊天内容
    val textarea = this.deepseekPage.locator("textarea[placeholder='给 DeepSeek 发送消息 ']")
    textarea.waitFor()
    textarea.fill(chatContext)

    //点击发送按钮
    val sendButton = this.deepseekPage.locator("div[class='ds-button__background']").last
    sendButton.click()

    println("发送对话")
  }





  override def delete(): Unit = {}

  override def llmName(): String = "deepseek"
  override def llmChatURL(): String = "https://chat.deepseek.com"
  override def llmOfficialWebsite(): String = "https://www.deepseek.com/"
}
