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
import zuk.sast.spring.controller.service.{ITushareStockService, QuerTushareStockDto, TushareStock_My_Impl, TushareStock_module_Impl, Tushare_All_Impl, Tushare_consolidation_Impl, _limit_down_Impl, _limit_up_Impl}
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
      case _limit_up_Impl.limit_up =>
        //涨停的股票
        this.tushareStockServiceList.asScala.filter(_.getType().equals(_limit_up_Impl.limit_up)).head.getStocks(dto)
      case _limit_down_Impl.limit_down =>
        //跌停的股票
        this.tushareStockServiceList.asScala.filter(_.getType().equals(_limit_down_Impl.limit_down)).head.getStocks(dto)
      case Tushare_consolidation_Impl.consolidation =>
        //黏合预测
        this.tushareStockServiceList.asScala.filter(_.getType().equals(Tushare_consolidation_Impl.consolidation)).head.getStocks(dto)
      case _=>
        this.tushareStockServiceList.asScala.filter(_.getType().equals(TushareStock_module_Impl.module)).head.getStocks(dto)
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
    limitUpMap.put("cls", _limit_up_Impl.limit_up)
    limitUpMap.put("name", _limit_up_Impl.limit_up_desc)
    list.append(limitUpMap)
    
    val limitDownMap = new util.HashMap[String, String]()
    limitDownMap.put("cls", _limit_down_Impl.limit_down)
    limitDownMap.put("name", _limit_down_Impl.limit_down_desc)
    list.append(limitDownMap)

    val consolidationMap = new util.HashMap[String, String]()
    consolidationMap.put("cls", Tushare_consolidation_Impl.consolidation)
    consolidationMap.put("name", Tushare_consolidation_Impl.consolidation_desc)
    list.append(consolidationMap)

    /**
     * 模型
     */
    TushareStock_module_Impl.module_names.foreach(clsName=>{
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
