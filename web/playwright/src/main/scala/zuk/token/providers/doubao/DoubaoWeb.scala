package zuk.token.providers.doubao

import com.alibaba.fastjson2.JSONObject
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
//    this.doubaoChatPage.waitForResponse()
  }

  override def onListenRequest(request: Request): Boolean = {

    val url = request.url()
    //https://www.doubao.com/chat/completion
    if(url.contains("doubao") && url.contains("completion")){
      val response = request.response()
      response.headers().asScala.map(e=>{
        s"${e._1}:${e._2}"
      }).foreach(l=>{
//        println(l)
      })
//      val responseText = response.text() //中文乱码
//      val bodyStr = new String(responseText.getBytes("ISO-8859-1"), StandardCharsets.UTF_8)
//      println(responseText)

//      response.body().take(100).foreach(b => {
//        print(f"${b & 0xff}%02X ")
//      })


//      println(response.body().length)
//      var bodyStr = new String(response.body(), "UTF-8")
//      println(bodyStr)
//
//      println(response.body().length)
//      bodyStr = new String(response.body(), "GBK")
//      println(bodyStr)
//
//      println(response.body().length)
//      bodyStr = new String(response.body(), "ISO-8859-1")
//      println(bodyStr)

//      val bodyStr = new String(response.text().getBytes("ISO-8859-1"), StandardCharsets.UTF_8)


      val data = response.body

      import org.mozilla.universalchardet.UniversalDetector

      val detector = new UniversalDetector(null)
      detector.handleData(data, 0, data.length)
      detector.dataEnd

      val charset = detector.getDetectedCharset

      System.out.println("编码: " + charset)

      val array = Array(
        "UTF-8",
        "UTF-16",
        "UTF-16LE",
        "UTF-16BE",
        "GBK",
        "GB2312",
        "GB18030",
        "ISO-8859-1",
        "US-ASCII",
        "Windows-1252",
        "Shift_JIS",
        "Big5",
        "EUC-KR",
        "KOI8-R"
        )

      array.foreach(e=>{

        try {

          print(s"编码：${e}")
//          val bodyStr = new String(response.text().getBytes(e), StandardCharsets.UTF_8)
//          val parseStr = parseProvider(bodyStr)

          val bodyStr = new String(response.body(), e)
          val parseStr = parseProvider(bodyStr)

          println("豆包回复内容：" + parseStr)
        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
        }
      })

    }
    true
  }


  override def parseProvider(responseText: String): String = {
    val stringBuilder = new StringBuilder()
    responseText.split("\n").filter(_.trim.startsWith("data:")).foreach(l=>{
      val json = l.substring("data:".size)
      val jsonObj = JSONObject.parseObject(json)
      val content = jsonObj.get("text")
      if(content!=null){
        stringBuilder.append(content)
      }

    })
    val str = stringBuilder.toString()
    println(str)
    str
  }

  override def delete(): Unit = {

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
