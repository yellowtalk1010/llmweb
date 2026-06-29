package zuk.token.providers.kimi

import com.alibaba.fastjson2.JSONObject
import com.microsoft.playwright.{Page, Request}
import zuk.token.TaskHandleFactory
import zuk.token.providers.{ChromeBrowser, IProviderToken}

import scala.jdk.CollectionConverters.*
/***
 * 月之暗面 kimi
 */
class KimiWeb extends IProviderToken{

  var kimiPage: Page = null
  var kimiChatTime = 0

  override def llmName(): String = "kimi"

  override def llmChatURL(): String = "https://www.kimi.com/?chat_enter_method=new_chat"

  private def isKimiChatPage(url: String) = {
    url.equals("https://www.kimi.com")
      || url.startsWith("https://www.kimi.com/chat")
      || (url.contains("https://www.kimi.com") && url.contains("chat_enter_method=new_chat"))
  }

  override def chat(chatContent: String): Unit = {
    this.kimiChatTime = this.kimiChatTime + 1
    if (this.kimiChatTime >= 30) {
      println("kimi执行30次，等待6分钟")
      Thread.sleep(6 * 60 * 1000)
      if (this.kimiPage != null) {
        this.kimiPage.close()
      }
      this.kimiPage = null
      this.kimiChatTime = 0
    }

    val browserContext = ChromeBrowser.browserContext
    if (browserContext == null) {
      return
    }

    val exist = browserContext.pages().asScala.map(_.url()).toSet.filter(e=>{isKimiChatPage(e)}).size > 0
    if (exist) {
      //如果存在，则输出全部url
      browserContext.pages().asScala.foreach(page => {
        val url = page.url()
        //println(s"kimi任务url:${url}")
        if (this.kimiPage == null && isKimiChatPage(url)
        ) {
          this.kimiPage = page
        }
      })
    }
    else {
      //创建一个新页面
      this.kimiPage = browserContext.newPage()
      //导航到登录页面
      this.kimiPage.navigate(llmChatURL())
    }

    println(s"kimi开始对话:${this.kimiPage.url()}")

    //向输入框中输入聊天内容
    val chatInput = this.kimiPage.locator("div.chat-input-editor")
    chatInput.waitFor()
    chatInput.click()
    chatInput.fill(chatContent)

    //点击发送按钮
    val sendButton = this.kimiPage.locator("div[class='send-button-container']").last
    sendButton.click()

    println(s"kimi发送对话")
    println(s"kimi等待回复")
  }

  override def onListenRequest(request: Request): Boolean = {
    val url = request.url()
    //https://www.kimi.com/apiv2/kimi.gateway.chat.v1.ChatService/Chat
    if(url.contains("kimi") && url.contains("kimi.gateway.chat.v1.ChatService") && url.contains("Chat")){
      println(s"kimi监听接口：${url}")
      val responseText = request.response().text()
      parseProvider(responseText)
    }
    true
  }

  override def delete(): Unit = {

  }

  override def parseProvider(responseText: String): String = {
    val array = responseText.toCharArray
    val splitStr = "" + array(0) + array(1) + array(2) + array(3)
    val ls = responseText.split(splitStr).toList
    val lines = ls.filter(e=>e.size>=2).map(l=>l.substring(1)).filter(l=>l.contains("\"op\":\"append\""))
    lines.foreach(println)
    val stringBuilder = new StringBuilder()
    lines.map(json=>{
      val jsonObj = JSONObject.parseObject(json)
      val block = jsonObj.get("block")
      if (block!=null) {
        block match {
          case blockJsonObj: JSONObject =>
            val textObj = blockJsonObj.get("text")
            textObj match {
              case textJsonObj: JSONObject =>
                val content = textJsonObj.get("content").asInstanceOf[String]
                stringBuilder.append(content)
              case _=>
            }
          case _=>
        }
      }
    })
    val str = stringBuilder.toString()
    str
  }

  override def run(): Unit = {
    while (true){
      try {
        println(s"队列长度:${TaskHandleFactory.TASK_QUEUE.size()}")
        val itask = TaskHandleFactory.TASK_QUEUE.poll()
        if(itask!=null){
          println(s"kimi执行任务id:${itask.id}")
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
}
