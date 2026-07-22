package zuk.sast.spring.controller

import com.alibaba.fastjson2.JSONObject
import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import TushareStockController.eliminate_str
import zuk.sast.spring.controller.component.{TushareAllStocks, TushareAllStocksCSVComponent, TushareConceptComponent, TushareInitMA4ModelMA5ModelComponent, TushareStockDailyDataComponent}
import zuk.sast.spring.controller.mapper.StockMapper
import zuk.sast.spring.controller.mapper.entity.StockEntity
import zuk.tu_share.dto.TsStock
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.utils.TopInstUtil

import java.text.SimpleDateFormat
import java.util
import java.util.concurrent.Executors
import java.util.{Date, Properties, UUID}
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object TushareStockController {
  val attention_str: String = "attention" //关注
  val buy_str: String = "buy" //购买
  val eliminate_str: String = "eliminate" //淘汰
}

class TushareStockControllerDTO extends StockEntity {
  @BeanProperty var conceptURL: String = null
  @BeanProperty var concept: String = null
  @BeanProperty var eastmoneyURL: String = null
  @BeanProperty var attention: String = null
  @BeanProperty var buy: String = null
  @BeanProperty var eliminate: String = null
  @BeanProperty var selectModel: String = null
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

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null

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

    //ma4次数
    val ma5List = this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR))

    val tsStockList = sets.toList.map(e=>{
        TushareAllStocks.getTsStock(e)
    }).filter(!_.isEmpty)
      .map(e=>{
        val dto = new TushareStockControllerDTO
        dto.selectModel = "我的"
        dto.stockCode = e.get.ts_code
        val codeList = ma5List.filter(_.stockCode.equals(e.get.ts_code)).sortBy(_.createtime).reverse
        dto.name = if(codeList.size==0) e.get.name else s"${e.get.name}【${codeList.size}次】${codeList.head.createtime}"
        if(dto.stockCode.startsWith("688")){
          dto.name = s"${dto.name}【科创】"
        }
        else if (dto.stockCode.startsWith("920")) {
          dto.name = s"${dto.name}【北交所】"
        }


        val topInstSize = TopInstUtil.existTopInst(dto.stockCode)
        if (topInstSize > 0) {
          dto.name = s"${dto.name}【龙虎榜${topInstSize}次】"
        }


        val optionTp3 = TushareStockDailyDataComponent.getIncreateRate(dto.stockCode)
        dto.remark = optionTp3.get._4
        dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
        dto.eastmoneyURL = e.get.eastmoneyURL
        dto.conceptURL = e.get.conceptURL
        dto.attention = ""
        if (attentionSet.contains(dto.stockCode)) {
          dto.attention = "已关注"
        }
        dto.buy = ""
        if(buySet.contains(dto.stockCode)){
          dto.buy = "已购买"
        }
        dto
      }).sortBy(e=>(e.buy, e.stockCode)).reverse.asJava

    tsStockList
  }

  /***
   * 获取全部股票信息
   * @return
   */
  private def getAll(desc: String): java.util.List[TushareStockControllerDTO] = {


    val allList = TushareAllStocks.getAll().map(e=>{
      val dto = new TushareStockControllerDTO
      dto.selectModel = "全部"
      dto.stockCode = e.ts_code
      dto.name = e.name
      dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
      dto.eastmoneyURL = e.eastmoneyURL
      dto.conceptURL = e.conceptURL
      dto.attention = ""
      dto
    })

    val splits = desc.split("&").map(_.trim)
    //
    val list = if (StringUtils.isNotBlank(desc)) {
      allList.filter(e => {
        val size = splits.filter(s=>{
          e.stockCode.contains(s) || e.name.contains(s) || e.concept.contains(s)
        }).size
        size == splits.size
//        e.stockCode.contains(desc) || e.name.contains(desc) || e.concept.contains(desc)
      })
    }
    else {
      allList
    }

    val num = 100
    val res = if (list.size > num) {
      list.take(num)
    }
    else {
      list
    }


    val attentionSet = getAllAttention()
    val buySet = getAllBuy()

    res.foreach(e => {

      if (attentionSet.contains(e.stockCode)) {
        e.attention = "已关注"
      }
      e.buy = ""
      if (buySet.contains(e.stockCode)) {
        e.buy = "已购买"
      }
      e
    })

    res.asJava

  }

  def getMa7(maStr: String, tradedate: String): java.util.List[TushareStockControllerDTO] = {
    log.info(s"getMa7, maStr:${maStr}, tradedate:${tradedate}")
    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

    val list = this.stockMapper.selectAll().asScala
      .filter(_.stockType.equals(maStr))
      .sortBy(e=>(e.createtime, e.stockCode))
      .reverse

    val list1 = list.map(entity => {
        val dto = new TushareStockControllerDTO
        dto.selectModel = entity.stockType
        dto.stockCode = entity.stockCode
        dto.name = entity.name
        if (dto.stockCode.startsWith("688")) {
          dto.name = s"${dto.name}【科创】"
        }
        else if (dto.stockCode.startsWith("920")) {
          dto.name = s"${dto.name}【北交所】"
        }

        val topInstSize = TopInstUtil.existTopInst(dto.stockCode)
        if (topInstSize > 0) {
          dto.name = s"${dto.name}【龙虎榜${topInstSize}次】"
        }

        dto.remark = entity.createtime
        dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
        val tsStock = new TsStock(entity.stockCode)
        dto.eastmoneyURL = tsStock.eastmoneyURL
        dto.conceptURL = tsStock.conceptURL
        if (attentionSet.contains(dto.stockCode)) {
          dto.attention = "已关注"
        }
        dto.buy = ""
        if (buySet.contains(dto.stockCode)) {
          dto.buy = "已购买"
        }
        dto.createtime = entity.createtime
        Some(dto)

      })
      .filter(!_.isEmpty)
      .map(_.get)
      .filter(e=>{
        if(StringUtils.isNotBlank(tradedate)){
          e.createtime.trim.equals(tradedate.trim)
        }
        else {
          true
        }
      })
      .asJava

      list1.asScala.groupBy(e=>e.stockCode).filter(e=>e._2.size>1).map(_._2).foreach(ls=>{
        ls.map(e=>{
          e.remark = s"${e.remark}【历史出现${ls.size}次】"
        })
      })

      list1
  }

  /***
   *
   * @return
   */
  def getMa(maStr: String, tradedate: String): java.util.List[TushareStockControllerDTO] = {

    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

    val list = this.stockMapper.selectAll().asScala
      .filter(_.stockType.equals(maStr))
      .groupBy(_.stockCode)
      .map(e=>{
        val ls = e._2.toList.sortBy(_.createtime).reverse
        val head = ls.head
        head.name = s"${head.name}【历史出现${ls.size}次${head.createtime}】"
        head
      })
      .toList.sortBy(_.createtime).reverse
      .map(entity=>{
        val dto = new TushareStockControllerDTO
        dto.selectModel = maStr
        dto.stockCode = entity.stockCode
        dto.name = entity.name
        if(dto.stockCode.startsWith("688")){
          dto.name = s"${dto.name}【科创】"
        }
        else if (dto.stockCode.startsWith("920")) {
          dto.name = s"${dto.name}【北交所】"
        }
        val optionTp3 = TushareStockDailyDataComponent.getIncreateRate(dto.stockCode)
        if(optionTp3.get._1 > 0
//          && optionTp3.get._1 < 120.0  //当前价位
//          && optionTp3.get._2 < 2.5  //翻倍
        ){
          dto.remark = optionTp3.get._4
          dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
          val tsStock = new TsStock(entity.stockCode)
          dto.eastmoneyURL = tsStock.eastmoneyURL
          dto.conceptURL = tsStock.conceptURL
          dto.remark = dto.remark + entity.remark
          if (attentionSet.contains(dto.stockCode)) {
            dto.attention = "已关注"
          }
          dto.buy = ""
          if (buySet.contains(dto.stockCode)) {
            dto.buy = "已购买"
          }

          Some(dto)
        }
        else {
          Option.empty
        }

      })
      .filter(!_.isEmpty).map(_.get)
      .asJava

    log.info(s"ma4总数据：${list.size()}")
    list

  }

  /***
   * 索取全部股票
   */
  @GetMapping(value = Array("all"))
  def all(desc: String, status: String, tradedate: String): util.Map[String, Object] = {
    log.info(s"索取全部股票:desc:${desc}, status:${status}, tradedate:${tradedate}")

    val list = status match {
      case "my" =>
        this.getMy()
      case "all" =>
        this.getAll(desc)
      case "ma4" =>
        this.getMa(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR, tradedate.replaceAll("-",""))
      case "ma5" =>
        this.getMa(TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR, tradedate.replaceAll("-",""))
      case "ma7" =>
        this.getMa7(TushareInitMA4ModelMA5ModelComponent.MA7_MODEL_STR, tradedate.replaceAll("-",""))
      case _=>
        new util.ArrayList[StockResultJson]()
    }

    val map = new util.HashMap[String, Object]()
    map.put("code", "success")
    map.put("data", list)

    map
  }

  /** *
   * 获取模型列表
   *
   * @return
   */
  @GetMapping(value = Array("moduleList"))
  def moduleList(): util.Map[String, Object] = {

    val list = ListBuffer[util.HashMap[String, String]]()

    val myMap = new util.HashMap[String, String]()
    myMap.put("cls", "my")
    myMap.put("name", "我的")
    list.append(myMap)

    val allMap = new util.HashMap[String, String]()
    allMap.put("cls", "all")
    allMap.put("name", "全部")
    list.append(allMap)

    val ma4Map = new util.HashMap[String, String]()
    ma4Map.put("cls", "ma4")
    ma4Map.put("name", "ma4")
    list.append(ma4Map)

    val ma5Map = new util.HashMap[String, String]()
    ma5Map.put("cls", "ma5")
    ma5Map.put("name", "ma5")
    list.append(ma5Map)

    val ma7Map = new util.HashMap[String, String]()
    ma7Map.put("cls", "ma7")
    ma7Map.put("name", "ma7")
    list.append(ma7Map)


    val map = new util.HashMap[String, Object]()
    map.put("code", "success")
    map.put("data", list.asJava)
    map
  }

  /** *
   * 移除购买
   */
  @GetMapping(value = Array("delete_stock"))
  def delete_stock(@RequestParam(value = "tsCode", required = false) tsCode: String,
                   @RequestParam(value = "stockType", required = false) stockType: String): util.Map[String, String] = synchronized {

    log.info(s"删除${stockType}, ${tsCode}, ${TushareAllStocks.getTsStock(tsCode).getOrElse(new TsStock).name}")

    stockType match {
      case TushareStockController.buy_str =>
        stockMapper.deleteByCode(tsCode, TushareStockController.buy_str)
      case TushareStockController.attention_str =>
        stockMapper.deleteByCode(tsCode, TushareStockController.attention_str)
      case TushareStockController.eliminate_str =>
        stockMapper.deleteByCode(tsCode, TushareStockController.eliminate_str)
      case _=>
    }

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
    log.info(s"添加${stockType}, ${tsCode}, ${TushareAllStocks.getTsStock(tsCode).getOrElse(new TsStock).name}")

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

    log.info(s"添加${stockType}, ${tsCode}, ${TushareAllStocks.getTsStock(tsCode).getOrElse(new TsStock).name}")
    if(this.stockMapper.selectAll().asScala.filter(s=>s.stockCode.equals(tsCode) && s.stockType.equals(stockType)).size == 0){
      val stockEntity: StockEntity = new StockEntity
      stockEntity.id = UUID.randomUUID().toString.replaceAll("-", "")
      stockEntity.stockCode = tsCode
      stockEntity.name = TushareAllStocks.getTsStock(tsCode).getOrElse(new TsStock).name
      stockEntity.stockType = stockType
      stockEntity.createtime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date)
      stockMapper.insert(stockEntity)
    }

  }
}
