package zuk.continue

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.lang3.StringUtils
import zuk.continue.SocketBinary.socket

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}
import java.net.Socket
import scala.collection.mutable

object SocketClient {

  val getIdeSettings_ = "getIdeSettings"
  val getControlPlaneSessionInfo_ = "getControlPlaneSessionInfo"
  val getIdeInfo_ = "getIdeInfo"
  val getWorkspaceDirs_ = "getWorkspaceDirs"

  val getIdeSettings = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getIdeSettings","data":{"remoteConfigSyncPeriod":60,"userToken":"","pauseCodebaseIndexOnStart":false,"continueTestEnvironment":"production"}}"""
  val getControlPlaneSessionInfo = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getControlPlaneSessionInfo"}"""
  val getIdeInfo = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getIdeInfo","data":{"ideType":"jetbrains","name":"IntelliJ IDEA 2024.1","version":"2024.1","remoteName":"local","extensionVersion":"1.0.52","isPrerelease":false}}"""
  val getWorkspaceDirs = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getWorkspaceDirs","data":["file:///D:/csv1"]}"""

  var map = mutable.HashMap[String, String]()

  def main(args: Array[String]): Unit = {
    val socket = new Socket("127.0.0.1", 3000)
    val socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
    var line = ""
    var times = 0
    while (true && times != 4){
      line = socketInput.readLine()
      if (StringUtils.isNotBlank(line)) {
        times = times + 1
        println(s"${times}，line:${line}")
      }
      Thread.sleep(2000)
    }

    map.map(_._2).foreach(line=>{
      socketOutput.write(line + "\n")
    })

    println("over.")
  }


  private def lineFilter(line: String) = {
    val obj = JSONObject.parse(line)
    if (obj != null) {
      val messageType = obj.get("messageType").asInstanceOf[String]
      val messageId = obj.get("messageId").asInstanceOf[String]
      messageType match
        case getIdeSettings_ =>
          val str = getIdeSettings.replaceAll("XXX-XXX-XXX-XXX", messageId)
          map.put(getIdeSettings_, str)
        case getControlPlaneSessionInfo_ =>
          val str = getControlPlaneSessionInfo.replaceAll("XXX-XXX-XXX-XXX", messageId)
          map.put(getControlPlaneSessionInfo_, str)
        case getIdeInfo_ =>
          val str = getIdeInfo.replaceAll("XXX-XXX-XXX-XXX", messageId)
          map.put(getIdeInfo_, str)
        case getWorkspaceDirs_ =>
          val str = getWorkspaceDirs.replaceAll("XXX-XXX-XXX-XXX", messageId)
          map.put(getWorkspaceDirs_, str)
        case _ =>
    }
  }

}
