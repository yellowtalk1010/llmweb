package zuk.token.providers.deepseek

import com.microsoft.playwright.{Page, Request, Response, Route}
import org.apache.commons.io.FileUtils
import zuk.token.TaskHandleFactory
import zuk.token.providers.deepseek.tasks.{DeepseekTask, DeepseekTask_easymoneyConcept}
import zuk.token.providers.{ChromeBrowser, IProviderToken, ITask}

import java.io.File
import java.security.MessageDigest
import java.util.UUID
import scala.jdk.CollectionConverters.*

class DeepseekWeb extends IProviderToken {

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


  override def onListenRequest(request: Request): Boolean = synchronized {
    try {

      val url = request.url()
      if(url.contains("deepseek") && url.contains("completion")){
        println(s"监听onListenResponse.url:${url}")
        val response = request.response()
        val text = response.text()

        val task = new DeepseekTask_easymoneyConcept()
        task.responseText = text
        task.parseProvider()

        FileUtils.write(new File("releases/"+UUID.randomUUID().toString.replaceAll("-", "")), task.parserText, "UTF-8")

        //println("")
//        TaskHandleFactory.ANSWER_LIST += task.parserText
      }
      true
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        false
    }
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
    println("等待回复")
    //Thread.sleep(30 * 1000)
  }


  override def delete(): Unit = {}

  override def llmName(): String = "deepseek"
  override def llmChatURL(): String = "https://chat.deepseek.com"
  override def llmOfficialWebsite(): String = "https://www.deepseek.com/"

  override def run(): Unit = {
    while (true){
      try {
        val itask = TaskHandleFactory.TASK_QUEUE.poll()
        println(s"队列长度:${TaskHandleFactory.TASK_QUEUE.size()}")
        if(itask!=null){
          chat(itask.chatContent)
        }
      }
      catch {
        case exception: Exception=>
          exception.printStackTrace()
      }
      Thread.sleep(30 * 1000) //每30秒执行一次
    }
  }

  /***
   * 这个方式容易出现阻塞导致不问题
   */
//  @Deprecated
//  override def onListenRoute(route: Route): Boolean = {
//    val request = route.request()
//    val url = request.url()
//    if(url.contains("/api/v0/") && url.contains("completion")){
//      isBreak = true
//      val headers = request.headers()
//      val authorization = headers.get("authorization")
//      val postData = request.postData() // 可能为 null
//
//      println(s"[intercept] url = ${url}")
//      println(s"[intercept] authorization = ${authorization}")
//      println(s"[intercept] postData = ${postData}")
//
//      if (postData != null && postData.contains("chat_session_id")) {
//        // 在这里解析参数
//        // val jsonObj = JSONObject.parseObject(postData)
//
//        // 直接取消，不让它真正发出去
//        println("[intercept] matched, abort request")
//        route.abort()
//
//        headers.asScala.map(h => {
//          val k = h._1
//          val v = h._2
//          s"${k}=${v}"
//        }).toList.foreach(println)
//
//        //        val updatePostData = postData.replaceAll("hi", content)
//        //        val deepseekClient = new DeepseekClient
//        //        deepseekClient.createCompletion(headers, updatePostData)
//
//      }
//
//      return true
//    }
//    else {
//      route.resume()
//    }
//    false
//  }

}
