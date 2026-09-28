package zuk.sast.spring.controller

import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import zuk.sast.spring.controller.component.*
import zuk.sast.spring.controller.mapper.StockMapper
import zuk.sast.spring.controller.mapper.entity.StockEntity
import zuk.sast.spring.controller.service.{ITushareStockService, QuerTushareStockDto, TushareStock_My_Impl, Tushare_All_Impl, Tushare_consolidation_Impl, Tushare_Stock_limit_down_Impl, Tushare_Stock_limit_up_Impl}
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.TsStock
import zuk.tu_share.module.MA4_Model
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, Dataset_top_Inst_dir, FenCi_Util, IncreateDecreateRateDescUtil}

import java.io.File
import java.text.SimpleDateFormat
import java.util
import java.util.concurrent.Executors
import java.util.{Date, UUID}
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

class TushareStockControllerDTO extends StockEntity {
  //概念url
  @BeanProperty var conceptURL: String = null
  //概念
  @BeanProperty var concept: String = null
  //东方财富url
  @BeanProperty var eastmoneyURL: String = null
  //关注
  @BeanProperty var attention: String = ""
  //购买
  @BeanProperty var buy: String = ""
  @BeanProperty var eliminate: String = null
  @BeanProperty var selectModel: String = null

  //龙虎榜
  @BeanProperty var topInstitutions: String = null

  //交易日期
  @BeanProperty var tradedate: String = ""
  
  //相似度
  @BeanProperty var similarity: String = ""
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
  private var stockMapper: StockMapper = null

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null
  
  @Autowired
  private var tushareStockServiceList: java.util.List[ITushareStockService] = null

  private val Executor_Service = Executors.newCachedThreadPool()

  @PostConstruct
  def init(): Unit = {


    Executor_Service.execute(() => {
       //TODO
    })
  }


  /***
   * 获取关注股票
   * @return
   */
  def getAllAttention(): Set[String] = {
    TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.attention_str)).map(_.stockCode).toSet
  }

  /**
   * 获取淘汰股票
   * @return
   */
  def getAllEliminate(): Set[String] = {
    TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.eliminate_str)).map(_.stockCode).toSet
  }

  /***
   * 获取购买股票
   * @return
   */
  def getAllBuy(): Set[String] = {
    TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.buy_str)).map(_.stockCode).toSet
  }

  def getMa7(maStr: String, selectedDateStart: String, selectedDateEnd: String): java.util.List[TushareStockControllerDTO] = {
    log.info(s"getMa7, maStr:${maStr}, selectedDateStart:${selectedDateStart}, selectedDateEnd:${selectedDateEnd}")
    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

    //仅取前1000条记录
    println(s"TushareInitMA4ModelMA5ModelComponent.getStockEntityList：${TushareInitMA4ModelMA5ModelComponent.getStockEntityList.size}")
    val ls = TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(maStr))
    val list = if(ls.size>1000){
      ls.take(1000)
    }
    else{
      ls
    }

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

        //计算历史出现的次数
        val totalList = list.filter(_.stockCode.equals(dto.stockCode)).sortBy(e=>(e.createtime)).reverse
        val totalSize = totalList.size
        if(totalSize > 1){
          dto.name = s"${dto.name}【历史总出现${totalSize}次最近${totalList.head.createtime}】"
        }

        //
        dto.tradedate = entity.createtime

        dto.topInstitutions = Dataset_top_Inst_dir.existTopInst(dto.stockCode)

        val optionTp3 = IncreateDecreateRateDescUtil.getDescription(dto.stockCode)

        dto.remark = optionTp3.get._4
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
//        Some(dto)
        dto
      })
//      .filter(!_.isEmpty)
//      .map(_.get)
    val list2 = list1.filter(e=>{
        if(StringUtils.isNotBlank(selectedDateStart) && StringUtils.isNotBlank(selectedDateEnd)){
          val start = if(selectedDateStart.trim.toLong <= selectedDateEnd.trim.toLong){
            selectedDateStart.trim.toLong
          }
          else {
            selectedDateEnd.trim.toLong
          }

          val end = if(selectedDateStart.trim.toLong <= selectedDateEnd.trim.toLong) {
            selectedDateEnd.trim.toLong
          }
          else {
            selectedDateStart.trim.toLong
          }

          start <= e.createtime.trim.toLong && e.createtime.trim.toLong <= end
        }
        else if (StringUtils.isNotBlank(selectedDateStart)) {
          e.createtime.trim.equals(selectedDateStart.trim)
        }
        else if (StringUtils.isNotBlank(selectedDateEnd)) {
          e.createtime.trim.equals(selectedDateEnd.trim)
        }
        else {
          true
        }
      })
      .asJava

    if(list2.size()>0){
      log.info(s"时间范围:${list2.asScala.head.createtime}至${list2.asScala.last.createtime}")
    }

    list2
  }

  /***
   * 索取全部股票
   */
  @GetMapping(value = Array("all"))
  def all(desc: String, status: String, selectedDateStart: String, selectedDateEnd: String): util.Map[String, Object] = {
    
    log.info(s"索取全部股票:desc:${desc}, status:${status}, selectedDateStart:${selectedDateStart}, selectedDateEnd:${selectedDateEnd}")
    val dto = QuerTushareStockDto(desc, status, selectedDateStart.replaceAll("-", ""), selectedDateEnd.replaceAll("-", ""))

    val list = status match {
      case TushareStock_My_Impl.`my` =>
        //购买和关注的股票
        this.tushareStockServiceList.asScala.filter(_.getType().equals(TushareStock_My_Impl.my)).head.getStocks(dto)
      case Tushare_All_Impl.`all` =>
        //A股全量股票
        this.tushareStockServiceList.asScala.filter(_.getType().equals(Tushare_All_Impl.all)).head.getStocks(dto)
      case Tushare_Stock_limit_up_Impl.limit_up =>
        //涨停的股票
        this.tushareStockServiceList.asScala.filter(_.getType().equals(Tushare_Stock_limit_up_Impl.limit_up)).head.getStocks(dto)
      case Tushare_Stock_limit_down_Impl.limit_down =>
        //跌停的股票
        this.tushareStockServiceList.asScala.filter(_.getType().equals(Tushare_Stock_limit_down_Impl.limit_down)).head.getStocks(dto)
      case Tushare_consolidation_Impl.consolidation =>
        this.tushareStockServiceList.asScala.filter(_.getType().equals(Tushare_consolidation_Impl.consolidation)).head.getStocks(dto)
      case _=>
        val ls = PassFactory.moduleList().map(_.getClass.getSimpleName.toUpperCase).filter(e=>{
          e.equals(status)
        })
        if(ls.size>0){
          this.getMa7(ls.head, selectedDateStart.replaceAll("-",""), selectedDateEnd.replaceAll("-",""))
        }
        else {
          new util.ArrayList[TushareStockControllerDTO]()
        }
    }

    val map = new util.HashMap[String, Object]()
    map.put("code", "success")
    map.put("data", list)
    map.put("keyword", FenCi_Util.createFenCi(list.asScala.map(e=>e.concept).toList))

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
    myMap.put("cls", TushareStock_My_Impl.my)
    myMap.put("name", TushareStock_My_Impl.my_desc)
    list.append(myMap)

    val allMap = new util.HashMap[String, String]()
    allMap.put("cls", Tushare_All_Impl.all)
    allMap.put("name", Tushare_All_Impl.all_desc)
    list.append(allMap)

    val limitUpMap = new util.HashMap[String, String]()
    limitUpMap.put("cls", Tushare_Stock_limit_up_Impl.limit_up)
    limitUpMap.put("name", Tushare_Stock_limit_up_Impl.limit_up_desc)
    list.append(limitUpMap)
    
    val limitDownMap = new util.HashMap[String, String]()
    limitDownMap.put("cls", Tushare_Stock_limit_down_Impl.limit_down)
    limitDownMap.put("name", Tushare_Stock_limit_down_Impl.limit_down_desc)
    list.append(limitDownMap)

    val consolidationMap = new util.HashMap[String, String]()
    consolidationMap.put("cls", Tushare_consolidation_Impl.consolidation)
    consolidationMap.put("name", Tushare_consolidation_Impl.consolidation_desc)
    list.append(consolidationMap)

    /**
     * 模型
     */
    PassFactory.moduleList().map(_.getClass.getSimpleName.toUpperCase).foreach(clsName=>{
      val map = new util.HashMap[String, String]()
      map.put("cls", clsName)
      map.put("name", clsName)
      list.append(map)
    })

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

    log.info(s"删除${stockType}, ${tsCode}, ${Dataset_all_stocks_csv_file.getTsStock(tsCode).getOrElse(new TsStock).name}")

    stockType match {
      case TushareInitMA4ModelMA5ModelComponent.buy_str =>
        stockMapper.deleteByCode(tsCode, TushareInitMA4ModelMA5ModelComponent.buy_str)
      case TushareInitMA4ModelMA5ModelComponent.attention_str =>
        stockMapper.deleteByCode(tsCode, TushareInitMA4ModelMA5ModelComponent.attention_str)
      case TushareInitMA4ModelMA5ModelComponent.eliminate_str =>
        stockMapper.deleteByCode(tsCode, TushareInitMA4ModelMA5ModelComponent.eliminate_str)
      case _=>
    }

    TushareInitMA4ModelMA5ModelComponent.clearStockEntityList()

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
    log.info(s"添加${stockType}, ${tsCode}, ${Dataset_all_stocks_csv_file.getTsStock(tsCode).getOrElse(new TsStock).name}")

    stockType match {
      case TushareInitMA4ModelMA5ModelComponent.buy_str =>
        //购买的股票默认关注
        add(tsCode, TushareInitMA4ModelMA5ModelComponent.buy_str)
        add(tsCode, TushareInitMA4ModelMA5ModelComponent.attention_str)
      case TushareInitMA4ModelMA5ModelComponent.attention_str =>
        //关注股票
        add(tsCode, TushareInitMA4ModelMA5ModelComponent.attention_str)
      case TushareInitMA4ModelMA5ModelComponent.eliminate_str =>
        //淘汰
        add(tsCode, TushareInitMA4ModelMA5ModelComponent.eliminate_str)
      case _ =>
    }

    TushareInitMA4ModelMA5ModelComponent.clearStockEntityList()

    val size = this.stockMapper.selectByCode(tsCode).asScala.filter(s => s.stockType.equals(stockType)).size

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

    log.info(s"添加${stockType}, ${tsCode}, ${Dataset_all_stocks_csv_file.getTsStock(tsCode).getOrElse(new TsStock).name}")
    if(this.stockMapper.selectByCode(tsCode).asScala.filter(s=>s.stockType.equals(stockType)).size == 0){
      val stockEntity: StockEntity = new StockEntity
      stockEntity.id = UUID.randomUUID().toString.replaceAll("-", "")
      stockEntity.stockCode = tsCode
      stockEntity.name = Dataset_all_stocks_csv_file.getTsStock(tsCode).getOrElse(new TsStock).name
      stockEntity.stockType = stockType
      stockEntity.createtime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date)
      stockMapper.insert(stockEntity)
    }

  }
}
