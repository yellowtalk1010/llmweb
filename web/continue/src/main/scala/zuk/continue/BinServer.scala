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
  val getWorkspaceDirs = """{"messageId":"XXX-XXX-XXX-XXX","messageType":"getWorkspaceDirs","data":["file:///D:/development/github/continue-1.5.11/continue-1.5.11/manual-testing-sandbox"]}"""

  val executorService: ExecutorService = Executors.newCachedThreadPool()

  private def runProcess(): Process = {
    val builder = new ProcessBuilder(path)
    builder.environment().asScala.map(e=>s"${e._1}=${e._2}").foreach(e=>{
      println(e)
    })
    builder.directory(new File(path).getParentFile).start()
  }

  def main(args: Array[String]): Unit = {

    val continueNeedTypes = List("getIdeSettings", "getControlPlaneSessionInfo", "getIdeInfo", "getWorkspaceDirs")
    val continueNeedTypeMap = scala.collection.mutable.HashMap[String, String]()

    val process = runProcess()

    val stdoutReader: BufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))
    executorService.execute(()=>{
      //val stdout = new java.lang.StringBuilder
      var line: String = stdoutReader.readLine()
      while (line != null) {
        //stdout.append(line).append("\n")
        line = stdoutReader.readLine()
        try{
          val obj = JSONObject.parse(line)
          if(obj!=null){
            val messageType = obj.get("messageType").asInstanceOf[String]
            val messageId = obj.get("messageId").asInstanceOf[String]
            if(continueNeedTypes.filter(e=>e.equals(messageType)).size>0){
              continueNeedTypeMap.put(messageType, messageId)
            }
          }
        }
        catch
          case exception: Exception => exception.printStackTrace()

        println(s"stdout:${line}")
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

    val bufferedWriter: BufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))
    executorService.execute(()=>{
      Log.parse()
      val reqLines = Log.requestLines
      reqLines.foreach(req=>{
        Thread.sleep(2000)
        println()
        bufferedWriter.write(req.toCharArray)
        println()
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
