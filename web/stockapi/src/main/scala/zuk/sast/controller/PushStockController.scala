package zuk.sast.controller

import com.alibaba.fastjson2.{JSONArray, JSONObject}
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

case class StockResultJson(){

  @BeanProperty var file: File = null

  @BeanProperty var area: String = ""
  @BeanProperty var modDesc: String = ""
  @BeanProperty var ts_code: String = ""
  @BeanProperty var turnoverRate: String = ""
  @BeanProperty var name: String = ""

  @BeanProperty var limitUp: String = ""
  @BeanProperty var industry: String = ""
  @BeanProperty var limitDown: String = ""
  @BeanProperty var modWinRate: String = ""
  @BeanProperty var modClsName: String = ""
}

/***
 * 股票推荐列表
 */
@RestController
@RequestMapping(value = Array("push_stocks"))
@Component
class PushStockController {

  private val log = LoggerFactory.getLogger(classOf[PushStockController])

  /***
   * 获取application.properties中的数据，股票json结果路径
   */
  @Value("${stock.result.json.path}")
  @BeanProperty
  private var stockResultJsonPath: String = null

  @GetMapping(value = Array("list"))
  def all(tradedate: String): util.Map[String, Object] = {

    val pro = System.getProperties
    this.stockResultJsonPath = "D:\\development\\github\\stockapi\\result_json"
    log.info(s"股票json结果路径：${this.stockResultJsonPath}")
    val file = new File(this.stockResultJsonPath)
    if(file.exists() && file.isDirectory){
      val jsonfiles = file.listFiles().filter(_.getName.endsWith(".json"))
      val simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
      var dateStr = simpleDateFormat.format(new Date())
      val filterJsonFiles = jsonfiles.filter(_.getName.startsWith(dateStr)).sortBy(_.getName).reverse
      log.info(s"\njson文件：\n${filterJsonFiles.map(_.getName).mkString("\n")}")

      val stockResultJsonList = filterJsonFiles.map(file=>{
        val array = JSONArray.parseArray(FileUtils.readFileToString(file, Charset.forName("UTF-8")), classOf[StockResultJson])
        array.asScala.map(e=>{
          e.file=file
          e
        }).sortBy(_.modWinRate).reverse
      })

      val modWinRateClsNames = stockResultJsonList.flatMap(e=>e).groupBy(_.modClsName).map(e=>(e._1, e._2.toList.head)).toList.sortBy(_._2.modWinRate).reverse.map(e=>(e._1, e._2.modWinRate))
      log.info(s"\n胜率：\n${modWinRateClsNames.map(e=>{s"${e._1},${e._2}"}).mkString("\n")}")

      val heads = stockResultJsonList.head
      val histories = stockResultJsonList.slice(1, stockResultJsonList.length).flatMap(e=>e).sortBy(_.modWinRate).reverse

      val pushStocks = modWinRateClsNames.map(_._1).map(clsName=>{
        val headList = heads.filter(_.modClsName.equals(clsName)).sortBy(_.turnoverRate).reverse
        val historyList = histories.filter(_.modClsName.equals(clsName)).groupBy(_.ts_code).map(e=>{
          if(e._2.size==1){
            e._2.head
          }
          else {
            e._2.sortBy(_.file.getName).head //取最早推荐
          }
        }).toList
          .filter(e=> !headList.map(_.ts_code).contains(e.ts_code)) //不能再headList中
          .sortBy(_.file.getName)
          .reverse
        (clsName, headList, historyList)
      })




//      val headJson = filterJsonFiles.head
//      val headJsonArray = JSONArray.parseArray(FileUtils.readFileToString(headJson, Charset.forName("UTF-8")), classOf[StockResultJson])
//      val headSortedArray = headJsonArray.asScala.sortBy(e=>(e.modWinRate, e.turnoverRate))(
//        Ordering.Tuple2(
//          Ordering.String.reverse, //降序
//          Ordering.String.reverse  //降序
//        ))
//
//      val historyJsons = filterJsonFiles.slice(1, filterJsonFiles.length)


      println()
    }
    else {
      log.info(s"${this.stockResultJsonPath}路径不存在")
    }
    val list = new util.ArrayList[Object]()
    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", list)
    map
  }
}
