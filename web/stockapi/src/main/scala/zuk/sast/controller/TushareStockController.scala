package zuk.sast.controller

import com.alibaba.fastjson2.JSONWriter.Feature
import com.alibaba.fastjson2.{JSONArray, JSONObject}
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.controller.component.TushareAllStocksCSVComponent
import zuk.tu_share.dto.TsStock

import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.concurrent.Executors
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

/***
 * 股票推荐列表
 */
@RestController
@RequestMapping(value = Array("stocks"))
@Component
class TushareStockController {

  private val log = LoggerFactory.getLogger(classOf[TushareStockController])

  @Autowired
  private var tushareAllStocksCSVComponent: TushareAllStocksCSVComponent = null

  /** *
   * 获取application.properties中的数据，股票json结果路径
   */
  @Value("${stock.result.json.path}")
  @BeanProperty
  private var stockResultJsonPath: String = null

  /***
   * 获取关注股票
   * @return
   */
  def getAllAttention(): Tuple2[File, Set[String]] = {
    val file = new File(s"${this.stockResultJsonPath}${File.separator}attention.jsons")
    log.info(s"关注数据文件路径:${file.getAbsolutePath}")
    if (!file.exists()) {
      log.error(s"${file.getAbsolutePath}文件不存在")
      file.mkdirs()
      file.createNewFile()
    }
    val sets = FileUtils.readLines(file, Charset.forName("UTF-8")).asScala.toSet
    (file, sets)
  }

  /***
   * 获取购买股票
   * @return
   */
  def getAllBuy(): Tuple2[File, Set[String]] = {
    val file = new File(s"${this.stockResultJsonPath}${File.separator}buys.jsons")
    log.info(s"购买数据文件路径:${file.getAbsolutePath}")
    if (!file.exists()) {
      log.error(s"${file.getAbsolutePath}文件不存在")
      file.mkdirs()
      file.createNewFile()
    }
    val sets = FileUtils.readLines(file, Charset.forName("UTF-8")).asScala.toSet
    (file, sets)
  }

  /***
   * 索取全部关注和购买的股票
   */
//  @GetMapping(value = Array("my"))
  def my(): util.Map[String, Object] = {

    //购买的股票
    val buy = getAllBuy()
    val buyFile = buy._1
    val buySet = buy._2

    //关注的股票
    val attention = getAllAttention()
    val attentionFile = attention._1
    val attentionSet = attention._2
    val sets = buySet ++ attentionSet
    val tsStockList = sets.toList.map(e=>{
      tushareAllStocksCSVComponent.getTsStock(e)
    }).filter(!_.isEmpty)
      .map(e=>{
        val stockResultJson = new StockResultJson
        stockResultJson.ts_code = e.get.ts_code
        stockResultJson.name = e.get.name
        stockResultJson.eastmoneyURL = e.get.getEastmoneyURL()
        stockResultJson.attention = ""
        if (attentionSet.contains(stockResultJson.ts_code)) {
          stockResultJson.attention = "已关注"
        }
        stockResultJson.buy = ""
        if(buySet.contains(stockResultJson.ts_code)){
          stockResultJson.buy = "已购买"
        }
        stockResultJson
      }).sortBy(e=>e.buy).reverse.asJava

    val result = new util.HashMap[String, Object]()
    result.put("data", tsStockList)
    result.put("code", "success")
    result
  }

  /***
   * 索取全部股票
   */
  @GetMapping(value = Array("all"))
  def all(desc: String, status: String): util.Map[String, Object] = {
    log.info(s"索取全部股票:${desc}, ${status}")

    if(StringUtils.isNotBlank(status) && status.equals("my")){
      return this.my()
    }

    val list = if(StringUtils.isNotBlank(desc)){
      tushareAllStocksCSVComponent.getAll().filter(e => {
        e.ts_code.contains(desc) || e.name.contains(desc)
      })
    }
    else {
      tushareAllStocksCSVComponent.getAll()
    }

    val res = if(list.size > 20){
      list.take(20)
    }
    else {
      list
    }

    val attentionSet = getAllAttention()._2
    val buySet = getAllBuy()._2

    val convertRes = res.map(e=>{
      val stockResultJson = new StockResultJson
      stockResultJson.ts_code = e.ts_code
      stockResultJson.name = e.name
      stockResultJson.eastmoneyURL = e.getEastmoneyURL()
      stockResultJson.attention = ""
      if (attentionSet.contains(stockResultJson.ts_code)) {
        stockResultJson.attention = "已关注"
      }
      stockResultJson.buy = ""
      if (buySet.contains(stockResultJson.ts_code)) {
        stockResultJson.buy = "已购买"
      }
      stockResultJson
    }).asJava

    val map = new util.HashMap[String, Object]()
    map.put("code", "success")
    map.put("data", convertRes)
    map
  }

  /** *
   * 移除购买
   */
  @GetMapping(value = Array("delete_buy"))
  def delete_buy(tsCode: String): util.Map[String, String] = synchronized {
    log.info(s"删除购买:${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")
    val all = getAllBuy()

    val file = all._1
    val set = all._2

    val result = new util.HashMap[String, String]()
    result.put("code", "success")
    result.put("desc", "成功")

    val list = set.filter(!_.equals(tsCode)).toList
    FileUtils.writeLines(file, list.sorted.asJava)

    result
  }

  /** *
   * 添加购买
   */
  @GetMapping(value = Array("add_buy"))
  def add_buy(tsCode: String): util.Map[String, String] = synchronized {
    log.info(s"添加购买:${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")

    this.add_attention(tsCode) //购买的股票，默认关注

    val all = getAllBuy()
    val file = all._1
    val set = all._2

    val result = new util.HashMap[String, String]()
    result.put("code", "success")
    if (set.contains(tsCode)) {
      result.put("desc", s"已存在，${tsCode}")
    }
    else {
      val list = set.toBuffer
      list += tsCode
      FileUtils.writeLines(file, list.sorted.asJava)
      result.put("desc", s"添加成功，${tsCode}")
    }
    log.info(JSONObject.toJSONString(result))
    result
  }

  /***
   * 移除关注
   */
  @GetMapping(value = Array("delete_attention"))
  def delete_attention(tsCode: String): util.Map[String, String] = synchronized {
    log.info(s"删除关注:${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")
    val all = getAllAttention()

    val file = all._1
    val set = all._2

    val result = new util.HashMap[String, String]()
    result.put("code", "success")
    result.put("desc", "成功")

    val list = set.filter(! _.equals(tsCode)).toList
    FileUtils.writeLines(file, list.sorted.asJava)

    result
  }

  /***
   * 添加关注
   */
  @GetMapping(value = Array("add_attention"))
  def add_attention(tsCode: String): util.Map[String, String] = synchronized {
    log.info(s"添加关注:${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")
    val all = getAllAttention()
    val file = all._1
    val set = all._2

    val result = new util.HashMap[String, String]()
    result.put("code", "success")
    if(set.contains(tsCode)){
      result.put("desc", s"已存在，${tsCode}")
    }
    else {
      val list = set.toBuffer
      list += tsCode
      FileUtils.writeLines(file, list.sorted.asJava)
      result.put("desc", s"添加成功，${tsCode}")
    }
    log.info(JSONObject.toJSONString(result))
    result
  }
}
