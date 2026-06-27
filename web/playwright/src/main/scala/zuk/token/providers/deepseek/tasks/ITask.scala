package zuk.token.providers.deepseek.tasks

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.lang3.StringUtils

import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

trait ITask(@BeanProperty val chatContent: String) {

  @BeanProperty var responseText: String = null
  @BeanProperty var finished = false

  def parse(): String = {
    println(s"分析结果：${responseText}")

    val data = "data:"
    val lines = responseText.split("\n").toList.filter(e=>StringUtils.isNotEmpty(e.trim) && e.trim.startsWith(data)).map(e=>{
      e.substring(data.size).trim
    }).map(_.trim)

    lines.foreach(println)

    val stringBuilder = new StringBuilder()
    lines.foreach(l=>{
      println(l)
      val jsonObj = JSONObject.parseObject(l)
      if(jsonObj.get("v")!=null && jsonObj.get("v").isInstanceOf[String]){
        stringBuilder.append(jsonObj.get("v"))
      }
    })

    println(stringBuilder)

    stringBuilder.toString()
  }

}
