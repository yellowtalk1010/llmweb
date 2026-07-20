package zuk.sast.spring.controller.component

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import TushareInitMA4ModelMA5ModelComponent.{MA4_MODEL_STR, MA5_MODEL_STR}
import zuk.sast.spring.controller.StockResultJson
import zuk.sast.spring.controller.mapper.StockMapper
import zuk.sast.spring.controller.mapper.entity.StockEntity
import zuk.tu_share.backtest.BackTestDto
import zuk.tu_share.dto.TsStock

import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
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

  def get_MD7_MODEL_LIST(): List[StockEntity] = synchronized {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.MA7_MODEL_STR)).toList
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

  private def createId(stockCode: String, stockType: String, createtime: String): String = {
    try {
      val text = s"${stockCode.trim.toUpperCase}${stockType.trim.toUpperCase}${createtime.trim.toUpperCase}"
      val md = MessageDigest.getInstance("MD5")
      val digest = md.digest(text.getBytes(StandardCharsets.UTF_8))
      val sb = new StringBuilder()
      digest.foreach(b=>{
        sb.append(String.format("%02x", b & 0xff))
      })
      sb.toString()
    }
    catch {
      case exception: Exception =>
        s"${UUID.randomUUID().toString.replaceAll("-","")}-error"
    }
  }

  /***
   * 初始化历史 MA4_MODEL 和 MA5_MODEL 模型生成的历史数据
   */
  def init_MODEL_BACK_TEST_RESULT(): Unit = synchronized {
    val file = new File("D:\\development\\github\\stockapi\\MODEL_BACK_TEST_RESULT.txt")
    if(!file.exists()){
      log.info(s"${file.getAbsolutePath} 不存在")
      return
    }

    //需要记录的模型
    val modelSet = List(
      TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR,
      TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR,
      TushareInitMA4ModelMA5ModelComponent.MA7_MODEL_STR
    ).map(_.toUpperCase).toSet
    val allEntitys = this.stockMapper.selectAll().asScala.filter(e=>modelSet.contains(e.stockType.toUpperCase))

    //
    val tradetimes = mutable.HashSet[String]()

    val lines = FileUtils.readLines(file, "UTF-8")
    val num = new AtomicLong(0)
    lines.asScala.foreach(line=>{

      val backTestDto = JSONObject.parseObject(line, classOf[BackTestDto])
      val stockType = backTestDto.stockType.trim.toUpperCase
      val stockCode = backTestDto.stockCode.trim
      val name = backTestDto.stockName.trim
      val time = backTestDto.tradedate.trim
      val id = createId(stockCode, stockType, time)

      tradetimes += time
      log.info(s"${num.getAndAdd(1)}/${lines.size()}, MODEL_BACK_TEST_RESULT.line:${line}，【${stockType}, ${stockCode}, ${name}, ${time}】")

      val existList = allEntitys.filter(e=>e.id.equals(id))
      if (existList.size == 0 && modelSet.contains(stockType)) {
        try {
          val stockEntity = new StockEntity
          stockEntity.id = id
          stockEntity.stockCode = stockCode
          stockEntity.name = name
          stockEntity.stockType = stockType
          stockEntity.createtime = time
          stockMapper.insert(stockEntity)

        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
        }

      }
      else {
        //log.info("已存在")
      }

    })

    val delList = allEntitys.filter(e=> !tradetimes.contains(e.createtime))
    log.info(s"待删除stock表中的记录总数:${delList.size}")
    delList.zipWithIndex.foreach(z=>{
      val e = z._1
      log.info(s"${z._2}删除stock表中记录:${e.id}, ${e.stockCode}, ${e.name}, ${e.stockType}, ${e.createtime}")
      this.stockMapper.deleteById(e.id)
    })

  }

}
