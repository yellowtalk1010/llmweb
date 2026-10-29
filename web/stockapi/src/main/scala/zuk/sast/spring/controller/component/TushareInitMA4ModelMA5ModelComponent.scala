package zuk.sast.spring.controller.component

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.spring.controller.StockResultJson
import zuk.sast.spring.controller.mapper.StockMapper
import zuk.sast.spring.controller.mapper.entity.StockEntity
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.TsStock
import zuk.tu_share.module.*

import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.{Date, UUID}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object TushareInitMA4ModelMA5ModelComponent {

  val attention_str: String = "attention" //关注
  val buy_str: String = "buy" //购买
  @Deprecated
  val eliminate_str: String = "eliminate" //淘汰
  
  /** *
   * 一次性加载stock表中的全部数据
   */
  var stockEntityList = new ListBuffer[StockEntity]()

  def getStockEntityList: List[StockEntity] = stockEntityList.toList

  def clearStockEntityList(): Unit = {
    stockEntityList.clear()
    val component = SpringApplicationUtil.context.getBean(classOf[TushareInitMA4ModelMA5ModelComponent])
    component.selectStockEntityAll()
  }

  def get_MODEL_LIST(modelType: String): List[StockEntity] = synchronized {
    stockEntityList.filter(_.stockType.equals(modelType)).toList
  }
}

@Component
class TushareInitMA4ModelMA5ModelComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareInitMA4ModelMA5ModelComponent])

  private val excecute: ExecutorService = Executors.newSingleThreadExecutor()

  @Autowired
  private var stockMapper: StockMapper = _

  @PostConstruct
  def init(): Unit = {

    excecute.execute(()=>{
      log.info("启动队列处理程序")
      //
      try {
        selectStockEntityAll()
      }
      catch {
        case exception: Exception =>
          exception.printStackTrace()
          log.error(exception.getMessage)
        case _ =>
      }
      finally {
        Thread.sleep(2000)
      }

    })
  }

  private def selectStockEntityAll(): List[StockEntity] = synchronized {
    if (TushareInitMA4ModelMA5ModelComponent.stockEntityList.isEmpty) {
      TushareInitMA4ModelMA5ModelComponent.stockEntityList ++= this.stockMapper.selectAll().asScala.sortBy(e => (e.createtime, e.stockCode)).reverse
      log.info(s"一次性加载stock表中的全部数据，总数:${TushareInitMA4ModelMA5ModelComponent.stockEntityList.size}")
    }
    TushareInitMA4ModelMA5ModelComponent.stockEntityList.toList
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
  
}
