package zuk.continue

import java.io.File
import java.net.{ServerSocket, Socket}
import java.util.concurrent.{ExecutorService, Executors}
import scala.jdk.CollectionConverters.*

object SocketBinary {

  val executor: ExecutorService = Executors.newCachedThreadPool

  var binaryProcess: Process = null

  var serverSocket: ServerSocket = null
  var socket: Socket = null

  def main(args: Array[String]): Unit = {

  }

  private def startBinary() = {
    val path = "C:/Users/visio/Downloads/continue-intellij-extension-1.0.52/continue-intellij-extension/core/win32-x64/continue-binary.exe"
    val builder = new ProcessBuilder(path)
    builder.environment().asScala.map(e => s"${e._1}=${e._2}").foreach(e => {
      println(e)
    })
    println("启动exe")
    binaryProcess = builder.directory(new File(path).getParentFile).start()
  }


  def startServer(port: Int): Unit = {
    serverSocket = new ServerSocket(port)
    println("服务端启动，监听端口: " + port)
    println("等待客户端连接")
    socket = serverSocket.accept
    println("客户端连接")
    startBinary()
    bridgeStreams()

  }

  def bridgeStreams() = {
    println("建立双向数据流")
  }

}
