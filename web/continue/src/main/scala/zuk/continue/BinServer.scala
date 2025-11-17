package zuk.continue

import com.alibaba.fastjson2.{JSON, JSONObject}

import java.io.{BufferedReader, BufferedWriter, File, InputStreamReader, OutputStreamWriter}
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object BinServer {

  val path = "C:\\Users\\visio\\Downloads\\continue-intellij-extension-1.0.52\\continue-intellij-extension\\core\\win32-x64\\continue-binary.exe"

  val getIdeSettings = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getIdeSettings","data":{"remoteConfigSyncPeriod":60,"userToken":"","pauseCodebaseIndexOnStart":false,"continueTestEnvironment":"production"}}"""
  val getControlPlaneSessionInfo = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getControlPlaneSessionInfo"}"""
  val getIdeInfo = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getIdeInfo","data":{"ideType":"jetbrains","name":"IntelliJ IDEA 2024.1","version":"2024.1","remoteName":"local","extensionVersion":"1.0.52","isPrerelease":false}}"""
  val getWorkspaceDirs = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getWorkspaceDirs","data":["file:///D:/csv1"]}"""

  val executorService: ExecutorService = Executors.newCachedThreadPool()

  private def runProcess(): Process = {
    val builder = new ProcessBuilder(path)
    builder.environment().asScala.map(e=>s"${e._1}=${e._2}").foreach(e=>{
      println(e)
    })
    builder.directory(new File(path).getParentFile).start()
  }

  def main(args: Array[String]): Unit = {

    val process = runProcess()

    val bufferedWriter: BufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))

    val stdoutReader: BufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))
    executorService.execute(()=>{
      //val stdout = new java.lang.StringBuilder
      var line: String = stdoutReader.readLine()
      while (line != null) {
        //stdout.append(line).append("\n")
        line = stdoutReader.readLine()
        println(s"stdout:${line}")
        try{
          val obj = JSONObject.parse(line)
          if(obj!=null){
            val messageType = obj.get("messageType").asInstanceOf[String]
            val messageId = obj.get("messageId").asInstanceOf[String]

            messageType match
              case "getIdeSettings" =>
                val str = getIdeSettings.replaceAll("XXX-XXX-XXX-XXX", messageId)
                println(str)
                bufferedWriter.write(str + "\n")
              case "getControlPlaneSessionInfo" =>
                val str = getControlPlaneSessionInfo.replaceAll("XXX-XXX-XXX-XXX", messageId)
                println(str)
                bufferedWriter.write(str + "\n")
              case "getIdeInfo" =>
                val str = getIdeInfo.replaceAll("XXX-XXX-XXX-XXX", messageId)
                println(str)
                bufferedWriter.write(str + "\n")
              case "getWorkspaceDirs" =>
                val str = getWorkspaceDirs.replaceAll("XXX-XXX-XXX-XXX", messageId)
                println(str)
                bufferedWriter.write(str + "\n")
              case _=>
          }
        }
        catch
          case exception: Exception => exception.printStackTrace()


      }
    })

    val stderrReader: BufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))
    executorService.execute(()=>{
//      val stderr = new java.lang.StringBuilder
      var line: String = stderrReader.readLine()
      while (line != null) {
        //stdout.append(line).append("\n")
        line = stdoutReader.readLine()
        println(s"stderr:${line}")
      }
    })


    executorService.execute(()=>{

      Log.parse()
      val reqLines = Log.requestLines.filter(l=>{
        val obj = JSONObject.parse(l)
        val messageType = obj.get("messageType").asInstanceOf[String]
        !List("getIdeSettings", "getControlPlaneSessionInfo", "getIdeInfo", "getWorkspaceDirs").contains(messageType)
      })
      reqLines.foreach(req=>{
        Thread.sleep(2000)
        bufferedWriter.write(req  + "\n")
        println(req  + "\n")
      })
    })

    val exitCode = process.waitFor

    println("等待")
    Thread.sleep(9999999)
    //executorService.awaitTermination(10000, TimeUnit.SECONDS)
    println("结束")
  }

}

class BinServer {



}
