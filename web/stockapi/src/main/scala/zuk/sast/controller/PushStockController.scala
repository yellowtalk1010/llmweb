package zuk.sast.controller

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

import java.io.File
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import scala.beans.BeanProperty

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
  def all(search: String, tradedate: String): util.Map[String, Object] = {

    val pro = System.getProperties
    this.stockResultJsonPath = "D:\\development\\github\\stockapi\\result_json"
    log.info(s"股票json结果路径：${this.stockResultJsonPath}")
    val file = new File(this.stockResultJsonPath)
    if(file.exists() && file.isDirectory){
      val jsonfiles = file.listFiles().filter(_.getName.endsWith(".json"))
      log.info(s"股票json结果总数：${jsonfiles.length}")
      val simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
      var dateStr = simpleDateFormat.format(new Date())
      val filterJsonFiles = jsonfiles.filter(_.getName.startsWith(dateStr)).sortBy(_.getName).reverse
      filterJsonFiles.foreach(file=>{
        println(file)
      })
      val headJson = filterJsonFiles.head
      val historyJsons = filterJsonFiles.slice(1, filterJsonFiles.length)
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
