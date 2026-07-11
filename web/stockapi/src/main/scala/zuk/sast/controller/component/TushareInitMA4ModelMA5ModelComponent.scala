package zuk.sast.controller.component

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.controller.StockResultJson
import zuk.sast.controller.component.TushareInitMA4ModelMA5ModelComponent.{MA4_MODEL_STR, MA5_MODEL_STR}
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.tu_share.backtest.BackTestDto
import zuk.tu_share.dto.TsStock

import java.io.File
import java.util
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ArrayBlockingQueue, ConcurrentHashMap, ExecutorService, Executors, LinkedBlockingQueue}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object TushareInitMA4ModelMA5ModelComponent {
  private val CacheStockEntityMap = new ConcurrentHashMap[String, java.util.List[StockEntity]]()
  val MA4_MODEL_STR: String = "MA4_MODEL"
  val MA5_MODEL_STR: String = "MA5_MODEL"
  val MA7_MODEL_STR: String = "MA7_MODEL"

//  private val queue = new LinkedBlockingQueue[StockResultJson]()
}

@Component
class TushareInitMA4ModelMA5ModelComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareInitMA4ModelMA5ModelComponent])

  private val excecute: ExecutorService = Executors.newSingleThreadExecutor()

  @Autowired
  private var stockMapper: StockMapper = _

  @Autowired
  private var tushareAllStocksCSVComponent: TushareAllStocksCSVComponent = _


  def get_MD4_MODEL_LIST(): List[StockEntity] = synchronized {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR)).toList
  }

  def get_MD5_MODEL_LIST(): List[StockEntity] = synchronized {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR)).toList
  }

  @PostConstruct
  def init(): Unit = {

    excecute.execute(()=>{
      log.info("启动队列处理程序")

      try {
        init_MODEL_BACK_TEST_RESULT()
        deleteRepeatMA4_MA5()
      }
      catch {
        case exception: Exception => exception.printStackTrace()
      }

    })
  }

  /***
   * 删除ma4，ma5在同日期中的相同元素
   */
  private def deleteRepeatMA4_MA5() = {
    log.info("删除ma4在同日期中的相同元素")
    this.stockMapper.selectAll().asScala.filter(e => List(MA4_MODEL_STR).equals(e.stockType))
      .groupBy(_.createtime)
      .map(_._2.toList)
      .foreach(dailyList => {
        dailyList.groupBy(_.stockCode).filter(_._2.size > 1).map(_._2).foreach(ls=>{
          log.info(s"重复${ls.size}条数据:${ls.head.stockType},${ls.head.createtime},${ls.head.stockCode}")
          for (i <- 1 until ls.size) {
            val e = ls(i)
            log.info(s"删除${MA4_MODEL_STR}中${e.createtime}重复元素：${JSONObject.toJSONString(e, Feature.LargeObject)}")
          }
        })
      })

    log.info("删除ma5在同日期中的相同元素")
    this.stockMapper.selectAll().asScala.filter(e => List(MA5_MODEL_STR).equals(e.stockType))
      .groupBy(_.createtime)
      .map(_._2.toList)
      .foreach(dailyList => {
        dailyList.groupBy(_.stockCode).filter(_._2.size > 1).map(_._2).foreach(ls=>{
          log.info(s"重复${ls.size}条数据:${ls.head.stockType},${ls.head.createtime},${ls.head.stockCode}")
          for (i <- 1 until ls.size) {
            val e = ls(i)
            log.info(s"删除${MA4_MODEL_STR}中${e.createtime}重复元素：${JSONObject.toJSONString(e, Feature.LargeObject)}")
          }
        })
      })

  }

  /***
   * 添加推荐数据
   * @param list
   */
//  def add_MA4_MA5_MODEL(stockResultList: List[StockResultJson]): Unit = synchronized {
//
//    try {
//      val ma4ma5List = stockResultList.filter(e=>List(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR, TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR).contains(e.modClsName.toUpperCase))
//      ma4ma5List.foreach(stockResult=>{
//        val stockCode= stockResult.ts_code
//        val stockType = stockResult.modClsName.toUpperCase
//        val dateStr = stockResult.fileName.substring(0, 8)
//
//        if(TushareInitMA4ModelMA5ModelComponent.CacheStockEntityMap.get(dateStr)==null){
//          //写入缓存
//          TushareInitMA4ModelMA5ModelComponent.CacheStockEntityMap.put(dateStr, this.stockMapper.select_MA4_MA5_By_Createtime(dateStr))
//        }
//        val list = TushareInitMA4ModelMA5ModelComponent.CacheStockEntityMap.get(dateStr).asScala
//          .filter(entity=>{
//            entity.stockCode.equals(stockCode) && entity.stockType.equals(stockType)
//          }).toList
//
//        if(list.size==0 && this.stockMapper.select_MA4_MA5_By_Createtime(dateStr).asScala.filter(e=>e.stockCode.equals(stockCode) && e.stockType.equals(stockType) && e.createtime.equals(dateStr)).size == 0){
//          val entity = new StockEntity
//          entity.id = UUID.randomUUID().toString.replaceAll("-", "")
//          entity.stockCode = stockCode
//          entity.stockType = stockType
//          entity.name = tushareAllStocksCSVComponent.getTsStock(stockCode).getOrElse(new TsStock).name
//          entity.createtime = dateStr
//          this.stockMapper.insert(entity)
//          //更新缓存
//          log.info(s"写入推荐MA4,MA5数据：${JSONObject.toJSONString(entity, Feature.LargeObject)}")
//          TushareInitMA4ModelMA5ModelComponent.CacheStockEntityMap.put(dateStr, this.stockMapper.select_MA4_MA5_By_Createtime(dateStr))
//        }
//      })
//
//    }
//    catch {
//      case exception: Exception =>
//        exception.printStackTrace()
//        log.error(exception.getMessage)
//    }
//  }

  /***
   * 初始化历史 MA4_MODEL 和 MA5_MODEL 模型生成的历史数据
   */
  def init_MODEL_BACK_TEST_RESULT(): Unit = synchronized {
    val file = new File("D:\\development\\github\\stockapi\\MODEL_BACK_TEST_RESULT.txt")
    if(!file.exists()){
      log.info(s"${file.getAbsolutePath} 不存在")
      return
    }

    val modelSet = List(
      TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR,
      TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR,
      TushareInitMA4ModelMA5ModelComponent.MA7_MODEL_STR
    ).map(_.toUpperCase).toSet
    val allEntitys = this.stockMapper.selectAll().asScala.filter(e=>modelSet.contains(e.stockType.toUpperCase))

    val lines = FileUtils.readLines(file, "UTF-8")
    val num = new AtomicLong(0)
    lines.asScala.foreach(line=>{

      val backTestDto = JSONObject.parseObject(line, classOf[BackTestDto])
      val stockType = backTestDto.stockType
      val stockCode = backTestDto.stockCode
      val name = backTestDto.stockName
      val time = backTestDto.tradedate

      val existList = allEntitys.filter(e => e.stockType.equals(stockType) && e.stockCode.equals(stockCode) && e.createtime.equals(time))
      if (existList.size == 0) {
        log.info(s"${num.getAndAdd(1)}/${lines.size()}, MODEL_BACK_TEST_RESULT.line:${line}，【${stockType}, ${stockCode}, ${name}, ${time}】")
        val stockEntity = new StockEntity
        stockEntity.id = UUID.randomUUID().toString.replaceAll("-", "")
        stockEntity.stockCode = stockCode.trim
        stockEntity.name = name.trim
        stockEntity.stockType = stockType.toUpperCase.trim
        stockEntity.createtime = time.trim
        stockMapper.insert(stockEntity)

        allEntitys += stockEntity
      }
      else {
        //log.info("已存在")
      }

    })
  }

}
