package zuk.continue

import java.io.{BufferedReader, File, InputStreamReader}
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}
import scala.jdk.CollectionConverters.*

object BinServer {

  val path = "C:\\Users\\visio\\Downloads\\continue-intellij-extension-1.0.52\\continue-intellij-extension\\core\\win32-x64\\continue-binary.exe"

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

    val  stdoutReader: BufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))
    val  stderrReader: BufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))

    val stdout = new java.lang.StringBuilder
    val stderr = new java.lang.StringBuilder
    var line: String = null

    line = stdoutReader.readLine()
    while (line != null) {
      stdout.append(line).append("\n")
      line = stdoutReader.readLine()
      println(s"stdout:${line}")
    }

    line = stderrReader.readLine()
    while (line!=null) {
      stderr.append(line).append("\n")
      line = stderrReader.readLine()
      println(s"stderr:${line}")
    }

    val exitCode = process.waitFor


//    process.errorReader()
//    process.inputReader()


//    executorService.execute(new Runnable(){
//      override def run(): Unit = {
//        while (true){
//          print(".")
//          val bufReader = process.errorReader()
//          if(bufReader.lines().toList.size()>0){
//            val line = bufReader.lines().toList.asScala.mkString("\n")
//            println("error:" + line)
//          }
//          Thread.sleep(200)
//        }
//      }
//    })

//    executorService.execute(new Runnable() {
//      override def run(): Unit = {
//        while (true) {
//          print("*")
//          val bufReader = process.inputReader()
//          if (bufReader.lines().toList.size() > 0) {
//            val line = bufReader.lines().toList.asScala.mkString("\n")
//            println("input:" + line)
//          }
//          Thread.sleep(200)
//        }
//      }
//    })

    println("等待")
    Thread.sleep(9999999)
    //executorService.awaitTermination(10000, TimeUnit.SECONDS)
    println("结束")
  }

}

class BinServer {



}
