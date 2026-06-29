package zuk.token.providers.doubao

import com.microsoft.playwright.{Page, Request}
import zuk.token.TaskHandleFactory
import zuk.token.providers.{ChromeBrowser, IProviderToken}

import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters.*
/***
 * 字节豆包
 */
class DoubaoWeb extends IProviderToken{

  /***
   * 豆包聊天界面
   */
  var doubaoChatPage: Page = null

  override def llmName(): String = "doubao" //字节豆包

  override def llmChatURL(): String = "https://www.doubao.com/chat"

  override def chat(chatContext: String): Unit = {
    val browserContext = ChromeBrowser.browserContext
    if (browserContext == null) {
      return
    }
    val exist = browserContext.pages().asScala.map(_.url()).toSet.filter(_.startsWith(llmChatURL())).size > 0
    if(exist){
      browserContext.pages().asScala.foreach(page=>{
        val url = page.url()
        if(this.doubaoChatPage==null && url.startsWith(llmChatURL())){
          this.doubaoChatPage = page
        }
      })
    }
    else {
      //新建一个页面
      this.doubaoChatPage = browserContext.newPage()
      //打开网页
      this.doubaoChatPage.navigate(llmChatURL())
    }
    println("豆包开始对话")
    //向输入框中输入聊天内容
    val textarea = this.doubaoChatPage.locator("textarea[placeholder='发消息...']")
    textarea.waitFor()
    textarea.fill(chatContext)

    Thread.sleep(2000)

    //点击发送按钮
    val sendButton = this.doubaoChatPage.locator("div[class='shrink-0 flex items-center h-[34px] send-btn-wrapper group']").last
    sendButton.click()

    println(s"豆包发送对话")
    println(s"豆包等待回复")
  }

  override def onListenRequest(request: Request): Boolean = {

    val url = request.url()
    //https://www.doubao.com/chat/completion
    if(url.contains("doubao") && url.contains("completion")){
      val response = request.response()
      response.headers().asScala.map(e=>{
        s"${e._1}:${e._2}"
      }).foreach(println)
      val responseText = response.text()
      val bodyStr = new String(responseText.getBytes("ISO-8859-1"), StandardCharsets.UTF_8)
      println("豆包回复内容：" + bodyStr)
    }
    true
  }

  override def delete(): Unit = {

  }

  override def parseProvider(responseText: String): String = {
    ""
  }

  override def run(): Unit = {

    while (true) {
      try {
        println(s"队列长度:${TaskHandleFactory.TASK_QUEUE.size()}")
        val itask = TaskHandleFactory.TASK_QUEUE.poll()
        if (itask != null) {
          println(s"豆包执行任务id:${itask.id}")
          chat(itask.chatContent)
        }
      }
      catch {
        case exception: Exception =>
          exception.printStackTrace()
      }
      Thread.sleep(30 * 1000) //每30秒执行一次
    }

  }
}
