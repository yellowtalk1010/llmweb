package zuk.token.providers.qianwen

import com.microsoft.playwright.{Page, Request}
import org.apache.commons.io.FileUtils
import zuk.token.providers.{ChromeBrowser, IProviderToken}

import java.io.File
import java.util.UUID
import scala.jdk.CollectionConverters.*

class QianwenWeb extends IProviderToken{

  var qianwenPage: Page = null

  var qianwenChatTime = 0

  override def llmName(): String = "qianwen"

  override def llmChatURL(): String = "https://www.qianwen.com/chat/"

  override def chat(chatContext: String): Unit = {
    this.qianwenChatTime = this.qianwenChatTime + 1
    if (this.qianwenChatTime >= 30) {
      println("qianwen执行30次，等待6分钟")
      Thread.sleep(6 * 60 * 1000)
      if (this.qianwenPage != null) {
        this.qianwenPage.close()
      }
      this.qianwenPage = null
      this.qianwenChatTime = 0
    }

    val browserContext = ChromeBrowser.browserContext
    if (browserContext == null) {
      return
    }
    val exist = browserContext.pages().asScala.map(_.url()).toSet.filter(_.startsWith(llmChatURL())).size > 0
    if (exist) {
      //如果存在，则输出全部url
      browserContext.pages().asScala.foreach(page => {
        val url = page.url()
        println(s"qianwen任务url:${url}")
        if (this.qianwenPage == null && page.url().startsWith(llmChatURL())) {
          this.qianwenPage = page
        }
      })
    }
    else {
      //创建一个新页面
      this.qianwenPage = browserContext.newPage()
      //导航到登录页面
      this.qianwenPage.navigate(llmChatURL())
    }

    println(s"qianwen开始对话")

    //向输入框中输入聊天内容
    val chatInput = this.qianwenPage.locator("div[data-chat-input-layout='true']")
    chatInput.click()
    chatInput.waitFor()
    chatInput.fill(chatContext)

    //点击发送按钮
    val sendButton = this.qianwenPage.locator("button[发送消息']").last
    sendButton.click()

    println(s"qianwen发送对话")
    println(s"qianwen等待回复")
  }

  override def onListenRequest(request: Request): Boolean = {
    try {

      val url = request.url()
      //println(s"qianwen监听的url:${url}")
      if(url.contains("qianwen") && url.contains("api/v2/chat")){
        println(s"qianwen监听onListenResponse.url:${url}")
        val response = request.response()

        val text = response.text()

        println(text)

        val parserText = parseProvider(text)

        val resultFile = new File(s"${task_ai_result_path}/" + s"${llmName()}_" + UUID.randomUUID().toString.replaceAll("-", ""))
        println(s"qianwen任务结果写入文件:${resultFile.getAbsolutePath}")
        FileUtils.write(resultFile, parserText, "UTF-8")

      }
      true
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        false
    }
  }

  override def delete(): Unit = {

  }

  override def parseProvider(responseText: String): String = {
    ""
  }

  override def run(): Unit = {

  }
}
