package zuk.continue

import org.apache.commons.lang3.StringUtils

import java.io.{BufferedReader, File, InputStreamReader, PrintWriter}
import java.net.{ServerSocket, Socket}
import java.util.concurrent.{ExecutorService, Executors}
import scala.jdk.CollectionConverters.*

object SocketBinary {

  val executor: ExecutorService = Executors.newCachedThreadPool

  var binaryProcess: Process = null
  var processOutput: BufferedReader = null
  var processInput: PrintWriter = null

  var serverSocket: ServerSocket = null
  var clientSocket: Socket = null

  def main(args: Array[String]): Unit = {
    startServer(3000)
  }

  private def startBinary() = {
    val path = "C:/Users/visio/Downloads/continue-intellij-extension-1.0.52/continue-intellij-extension/core/win32-x64/continue-binary.exe"
    val builder = new ProcessBuilder(path)
//    builder.environment().asScala.map(e => s"${e._1}=${e._2}").foreach(e => {
//      println(e)
//    })
//    println("启动exe")
    binaryProcess = builder.directory(new File(path).getParentFile).start()
    processOutput = new BufferedReader(new InputStreamReader(binaryProcess.getInputStream()))
    processInput = new PrintWriter(binaryProcess.getOutputStream(), true)
  }


  def startServer(port: Int): Unit = {
    serverSocket = new ServerSocket(port)
    println("服务端启动，监听端口: " + port)
    println("等待客户端连接")
    clientSocket = serverSocket.accept
    println(s"客户端连接:${clientSocket.getLocalSocketAddress}")
    startBinary()
    bridgeStreams()

  }

  def bridgeStreams() = {
    println("建立双向数据流")
    executor.execute(()=>{
      try {
        //exe的输出转给socketClient
        val socketOutput = new PrintWriter(clientSocket.getOutputStream(), true)
        while (true) {
          val line = processOutput.readLine() //读取exe
          if (StringUtils.isNotBlank(line)) {
//            println(s"exe->socket:${line}")
            socketOutput.println(line + "\n") //写入socket
          }
          Thread.sleep(200)
        }
      }
      catch
        case exception: Exception => exception.printStackTrace()
    })

    executor.execute(()=>{
      try {
        //socket的输入转给exe
        val socketInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
        while (true) {
          val line = socketInput.readLine()
          if (StringUtils.isNotBlank(line)) {
            println(s"socket->exe:${line}")
            processInput.write(line + "\n")
          }
          Thread.sleep(200)
        }
      }
      catch
        case exception: Exception => exception.printStackTrace()
    })

  }

}
