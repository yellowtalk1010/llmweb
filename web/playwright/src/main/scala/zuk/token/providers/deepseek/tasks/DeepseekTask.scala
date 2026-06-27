package zuk.token.providers.deepseek.tasks

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.lang3.StringUtils
import zuk.token.providers.ITask

trait DeepseekTask extends ITask {

  /***
   * deepseek解析策略
   */
  override def parseProvider(): String = {
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

    parserText = stringBuilder.toString()
    parserText
  }

}
