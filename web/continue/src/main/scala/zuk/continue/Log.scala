package zuk.continue

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.io.FileUtils

import scala.jdk.CollectionConverters.*
import java.io.File
import scala.collection.mutable.ListBuffer

object Log {

  val REQUEST = "request"
  val REPONSE = "reponse"

  val requestLines = ListBuffer[String]()
  val responseLines = ListBuffer[String]()

  /**
   * 分析continue.log日志
   * @param args
   */
  def main(args: Array[String]): Unit = {
    parse()
  }

  def parse(): Unit = {
    val logFile = new File("continue/src/main/resources/continue.log")
    println(s"${logFile.getAbsolutePath}, ${logFile.exists()}")
    val lines = FileUtils.readLines(logFile, "utf-8").asScala.map(l=>{
      val pres = List("handleMessage:", "request:").filter(e=>l.startsWith(e))
      if(pres.size>0){
        (REQUEST, l.substring(pres.head.length))
      }
      else {
        (REPONSE, l)
      }
    })
    println(lines.size)
    requestLines ++= lines.filter(_._1.equals(REQUEST)).map(_._2)
    responseLines ++= lines.filter(_._1.equals(REPONSE)).map(_._2)

    val requestObjs = requestLines.map(e=>{
      try{
        JSONObject.parseObject(e)
      }
      catch
        case exception: Exception =>
          exception.printStackTrace()
          null
    }).filter(_!=null)
    val responseObjs = responseLines.map(e=>{
      try{
        JSONObject.parseObject(e)
      }
      catch
        case exception: Exception =>
          println(e)
          exception.printStackTrace()
          null
    }).filter(_!=null)


    println(s"完成： ${requestObjs.size}, ${responseObjs.size}")

    val messageTypes = (requestObjs ++ responseObjs).map(_.get("messageType")).toSet
    println("消息类型总数：" + messageTypes.size)
    messageTypes.foreach(println)

    println("开始配对")
    val resIds = ListBuffer[String]()
    requestObjs.map(req=>{
      val reqId = req.get("messageId").asInstanceOf[String]
      val resObjs = responseObjs.filter(res=>{
        val resId = res.get("messageId").asInstanceOf[String]
        resIds += resId
        resId!=null && resId.equals(reqId)
      })
      (req, resObjs)
    }).foreach(tp2=>{
      println("\n\n")
      val reqStr = JSONObject.toJSONString(tp2._1)
      println(reqStr)
      tp2._2.foreach(res=>{
        val resStr = JSONObject.toJSONString(res)
        println(resStr)
      })
    })

    println(resIds.toSet.size)
    println("未配对的回复")
    responseObjs.filter(res=>{
      val resId = res.get("messageId").asInstanceOf[String]
      !resIds.contains(resId)
    }).foreach(res=>{
      val resStr = JSONObject.toJSONString(res)
      println(resStr)
    })

    println("over.")
  }

}
