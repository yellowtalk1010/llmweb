package zuk.sast.controller

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import zuk.sast.controller.component.TushareAllStocksCSVComponent
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.tu_share.dto.TsStock

import java.text.SimpleDateFormat
import java.util
import java.util.{Date, UUID}
import scala.jdk.CollectionConverters.*

object TushareStockController {
  val attention_str: String = "attention" //关注
  val buy_str: String = "buy" //购买
  val eliminate_str: String = "eliminate" //淘汰
}

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

  @Autowired
  private var stockMapper: StockMapper = null



  /***
   * 获取关注股票
   * @return
   */
  def getAllAttention(): Set[String] = {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareStockController.attention_str)).map(_.stockCode).toSet
  }

  /**
   * 获取淘汰股票
   * @return
   */
  def getAllEliminate(): Set[String] = {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareStockController.eliminate_str)).map(_.stockCode).toSet
  }

  /***
   * 获取购买股票
   * @return
   */
  def getAllBuy(): Set[String] = {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareStockController.buy_str)).map(_.stockCode).toSet
  }

  /***
   * 索取全部关注和购买的股票
   */
  def my(): util.Map[String, Object] = {

    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

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
      this.my()
    }
    else {

      val list = if (StringUtils.isNotBlank(desc)) {
        tushareAllStocksCSVComponent.getAll().filter(e => {
          e.ts_code.contains(desc) || e.name.contains(desc)
        })
      }
      else {
        tushareAllStocksCSVComponent.getAll()
      }

      val res = if (list.size > 20) {
        list.take(20)
      }
      else {
        list
      }

      val attentionSet = getAllAttention()
      val buySet = getAllBuy()

      val convertRes = res.map(e => {
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

  }

  /** *
   * 移除购买
   */
  @GetMapping(value = Array("delete_stock"))
  def delete_stock(@RequestParam(value = "tsCode", required = false) tsCode: String,
                   @RequestParam(value = "stockType", required = false) stockType: String): util.Map[String, String] = synchronized {

    log.info(s"删除${stockType}, ${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")

    stockType match {
      case TushareStockController.buy_str =>
        stockMapper.deleteByCode(tsCode, TushareStockController.buy_str)
      case TushareStockController.attention_str =>
        stockMapper.deleteByCode(tsCode, TushareStockController.attention_str)
      case TushareStockController.eliminate_str =>
        stockMapper.deleteByCode(tsCode, TushareStockController.eliminate_str)
      case _=>
    }

    val set = getAllBuy()

    val result = new util.HashMap[String, String]()
    result.put("code", "success")
    result.put("desc", "成功")

    result
  }

  /** *
   * 添加购买
   */
  @GetMapping(value = Array("add_stock"))
  def add_stock(@RequestParam(value = "tsCode", required = false) tsCode: String,
                @RequestParam(value = "stockType", required = false) stockType: String): util.Map[String, String] = synchronized {
    log.info(s"添加${stockType}, ${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")

    stockType match {
      case TushareStockController.buy_str =>
        //购买的股票默认关注
        add(tsCode, TushareStockController.buy_str)
        add(tsCode, TushareStockController.attention_str)
      case TushareStockController.attention_str =>
        //关注股票
        add(tsCode, TushareStockController.attention_str)
      case TushareStockController.eliminate_str =>
        //淘汰
        add(tsCode, TushareStockController.eliminate_str)
      case _ =>
    }

    val size = this.stockMapper.selectAll().asScala.filter(s => s.stockCode.equals(tsCode) && s.stockType.equals(stockType)).size

    val result = new util.HashMap[String, String]()
    result.put("code", "success")
    if (size > 0) {
      result.put("desc", "成功")
    }
    else {
      result.put("desc", "失败")
    }
    result
  }


  /***
   * 添加
   */
  private def add(tsCode: String, stockType: String): Unit = synchronized {

    log.info(s"添加${stockType}, ${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")
    if(this.stockMapper.selectAll().asScala.filter(s=>s.stockCode.equals(tsCode) && s.stockType.equals(stockType)).size == 0){
      val stockEntity: StockEntity = new StockEntity
      stockEntity.id = UUID.randomUUID().toString.replaceAll("-", "")
      stockEntity.stockCode = tsCode
      stockEntity.name = tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name
      stockEntity.stockType = stockType
      stockEntity.createtime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date)
      stockMapper.insert(stockEntity)
    }

  }
}
