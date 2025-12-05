package zuk

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.lang3.StringUtils

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket
import java.util.concurrent.{ExecutorService, Executors, LinkedBlockingQueue}
import scala.beans.BeanProperty

/***
 * 大模型调用客户端
 */
object LLMClient {

  val xxxxxxxxx = "XXX-XXX-XXX"

  val executor: ExecutorService = Executors.newCachedThreadPool
  var socketInput: BufferedReader = null
  var socketOutput: PrintWriter = null

  val queue = new LinkedBlockingQueue[LlmStreamChat]()

  /***
   * 处理读取到的数据
   */
  def handleReadLine(line: String): Unit = {
    try {
      val obj = JSONObject.parseObject(line)
      val messageType = obj.get("messageType").asInstanceOf[String]
      val messageId = obj.get("messageId").asInstanceOf[String]
      messageType match
        case "llm/streamChat" =>
          val obj: LlmStreamChat = JSONObject.parseObject(line, classOf[LlmStreamChat])
          if(!obj.data.done){
            val content = obj.data.content.content
            println(s">>>>>>>>>>>>>>>>>>>>>>>${content}")
            queue.put(obj)
          }
        case _=>
    }
    catch
      case exception: Exception =>
  }

  def handleWriteLine(line: String): Unit = {
    try {
      println(s"write:${line}")
      socketOutput.println(line)
      socketOutput.flush()
    }
    catch
      case exception: Exception =>
  }

  /***
   * 初始化
   */
  def init(): Unit = {
    val socketClient = new Socket("127.0.0.1", 1010)
    socketInput = new BufferedReader(new InputStreamReader(socketClient.getInputStream))
    socketOutput = new PrintWriter(socketClient.getOutputStream(), true)

    executor.execute(() => {
      while (true) {
        try {
          Thread.sleep(20)
          val line = socketInput.readLine()
          if (StringUtils.isNotBlank(line)) {
            println(s"read:${line}")
            handleReadLine(line)
          }
        }
        catch
          case exception: Exception => exception.printStackTrace()
      }
    })
    println("完成LLM连接")
  }

  def testWrite(messageId: String, codeContent: String): Unit = {
    try {
      val llm = """{"messageId":"XXX-XXX-XXX","messageType":"llm/streamChat","data":{"completionOptions":{"reasoning":false},"title":"DeepSeek Coder","messages":[{"role":"system","content":"你是一个资深的c/c++代码审计专家，主要审计代码中编码风格，编码安全漏洞问题。"},{"role":"assistant","content":"分析C/C++代码，让代码没有缺陷和漏洞，更好更安全。\n\n\n\n"},{"role":"user","content":"\n\n\n\n``` """ + codeContent + """ \n\n\n\n```"},{"role":"user","content":"分析这段代码，中文回复"}],"messageOptions":{"precompiled":true}}}"""
      val l = llm.replaceAll(xxxxxxxxx, messageId)
      handleWriteLine(l)
    }
    catch
      case exception: Exception =>
  }


  case class ContentData(@BeanProperty role: String,
                    @BeanProperty content: String)

  case class Data(@BeanProperty done: Boolean,
              @BeanProperty content: ContentData,
              @BeanProperty status: String)

  case class LlmStreamChat(@BeanProperty messageType: String,
                      @BeanProperty data: Data,
                      @BeanProperty messageId: String)
}
