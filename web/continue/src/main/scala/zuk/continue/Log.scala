package zuk.continue

import org.apache.commons.io.FileUtils
import scala.jdk.CollectionConverters.*
import java.io.File

object Log {

  /**
   * 分析continue.log日志
   * @param args
   */
  def main(args: Array[String]): Unit = {
    val logFile = new File("continue/src/main/resources/continue.log")
    println(s"${logFile.getAbsolutePath}, ${logFile.exists()}")
    val lines = FileUtils.readLines(logFile, "utf-8").asScala.map(l=>{
      val pres = List("handleMessage:", "request:").filter(e=>l.startsWith(e))
      if(pres.size>0){
        l.substring(pres.head.length)
      }
      else {
        l
      }
    })
    println(lines.size)
    lines.foreach(println)
  }

}
