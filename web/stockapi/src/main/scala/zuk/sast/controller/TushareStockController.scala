package zuk.sast.controller

import com.alibaba.fastjson2.JSONObject
import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import zuk.sast.controller.TushareStockController.eliminate_str
import zuk.sast.controller.component.{TushareAllStocksCSVComponent, TushareInitMA4ModelcCompont}
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.tu_share.dto.TsStock

import java.text.SimpleDateFormat
import java.util
import java.util.concurrent.Executors
import java.util.{Date, UUID}
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

object TushareStockController {
  val attention_str: String = "attention" //关注
  val buy_str: String = "buy" //购买
  val eliminate_str: String = "eliminate" //淘汰
}

class TushareStockControllerDTO extends StockEntity {
  @BeanProperty var eastmoneyURL: String = null
  @BeanProperty var attention: String = null
  @BeanProperty var buy: String = null
  @BeanProperty var eliminate: String = null
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

  private val Executor_Service = Executors.newCachedThreadPool()

  @PostConstruct
  def init(): Unit = {

    Executor_Service.execute(() => {

      try {
        val dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date())
        this.stockMapper.selectAll().asScala.filter(e => e.stockType.equals(eliminate_str) && !e.createtime.contains(dateStr)).foreach(s=>{
          log.info(s"删除推荐淘汰后的股票:${s.stockCode}, ${s.name}, ${s.stockType}, ${s.createtime}")
          stockMapper.deleteById(s.id)
        })
      }
      catch {
        case exception: Exception =>
          exception.printStackTrace()
          log.error(exception.getMessage)
        case _ =>
      }

    })

  }


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
  def getMy(): util.List[TushareStockControllerDTO] = {

    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

    val sets = buySet ++ attentionSet

    val tsStockList = sets.toList.map(e=>{
      tushareAllStocksCSVComponent.getTsStock(e)
    }).filter(!_.isEmpty)
      .map(e=>{
        val dto = new TushareStockControllerDTO
        dto.stockCode = e.get.ts_code
        dto.name = e.get.name
        dto.eastmoneyURL = e.get.getEastmoneyURL()
        dto.attention = ""
        if (attentionSet.contains(dto.stockCode)) {
          dto.attention = "已关注"
        }
        dto.buy = ""
        if(buySet.contains(dto.stockCode)){
          dto.buy = "已购买"
        }
        dto
      }).sortBy(e=>e.buy).reverse.asJava

    tsStockList
  }

  /***
   * 获取全部股票信息
   * @return
   */
  private def getAll(desc: String): java.util.List[TushareStockControllerDTO] = {
    val list = if (StringUtils.isNotBlank(desc)) {
      tushareAllStocksCSVComponent.getAll().filter(e => {
        e.ts_code.contains(desc) || e.name.contains(desc)
      })
    }
    else {
      tushareAllStocksCSVComponent.getAll()
    }

    val num = 20
    val res = if (list.size > num) {
      list.take(num)
    }
    else {
      list
    }

    val attentionSet = getAllAttention()
    val buySet = getAllBuy()

    val convertRes = res.map(e => {
      val dto = new TushareStockControllerDTO
      dto.stockCode = e.ts_code
      dto.name = e.name
      dto.eastmoneyURL = e.getEastmoneyURL()
      dto.attention = ""
      if (attentionSet.contains(dto.stockCode)) {
        dto.attention = "已关注"
      }
      dto.buy = ""
      if (buySet.contains(dto.stockCode)) {
        dto.buy = "已购买"
      }
      dto
    }).asJava

    convertRes

  }


  /***
   *
   * @return
   */
  def getMa4(): java.util.List[TushareStockControllerDTO] = {

    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

    val list = this.stockMapper.selectAll().asScala
      .filter(_.stockType.equals(TushareInitMA4ModelcCompont.MA4_MODEL_STR))
      .groupBy(_.stockCode)
      .map(e=>{
        val ls = e._2.toList.sortBy(_.createtime).reverse
        val head = ls.head
        head.name = s"${head.name}【${ls.size}次】${head.createtime}"
        head
      })
      .toList.sortBy(_.createtime).reverse
      .map(entity=>{
        val dto = new TushareStockControllerDTO
        dto.stockCode = entity.stockCode
        dto.name = entity.name
        val tsStock = new TsStock()
        tsStock.ts_code = entity.stockCode
        dto.eastmoneyURL = tsStock.getEastmoneyURL()
        dto.remark = entity.remark
        if (attentionSet.contains(dto.stockCode)) {
          dto.attention = "已关注"
        }
        dto.buy = ""
        if (buySet.contains(dto.stockCode)) {
          dto.buy = "已购买"
        }

        dto
      })
      .asJava

    log.info(s"ma4总数据：${list.size()}")
    list

  }

  /***
   * 索取全部股票
   */
  @GetMapping(value = Array("all"))
  def all(desc: String, status: String): util.Map[String, Object] = {
    log.info(s"索取全部股票:${desc}, ${status}")

    val list = status match {
      case "my" =>
        this.getMy()
      case "all" =>
        this.getAll(desc)
      case "ma4" =>
        this.getMa4()
      case _=>
        new util.ArrayList[StockResultJson]()
    }

    val map = new util.HashMap[String, Object]()
    map.put("code", "success")
    map.put("data", list)

    map
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
