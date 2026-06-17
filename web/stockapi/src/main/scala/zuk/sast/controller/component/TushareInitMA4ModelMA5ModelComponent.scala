package zuk.sast.controller.component

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.controller.StockResultJson
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.tu_share.dto.TsStock

import java.io.File
import java.util
import java.util.UUID
import java.util.concurrent.{ArrayBlockingQueue, ConcurrentHashMap, ExecutorService, Executors, LinkedBlockingQueue}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object TushareInitMA4ModelMA5ModelComponent {
  private var stockEntityList = new ConcurrentHashMap[String, java.util.List[StockEntity]]()
  val MA4_MODEL_STR: String = "MA4_MODEL"
  val MA5_MODEL_STR: String = "MA5_MODEL"

//  private val queue = new LinkedBlockingQueue[StockResultJson]()
}

@Component
class TushareInitMA4ModelMA5ModelComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareInitMA4ModelMA5ModelComponent])

//  private val excecute: ExecutorService = Executors.newSingleThreadExecutor()

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
//    excecute.execute(()=>{
//      log.info("启动队列处理程序")
//      while (true){
//        try {
//          val ele = TushareInitMA4ModelMA5ModelComponent.queue.poll() //从队列中取元素，如果为空，则等待
//          if(ele==null){
//            Thread.sleep(1000)
//          }
//          else {
//            val dateStr = ele.fileName.substring(0, 8)
//            if (TushareInitMA4ModelMA5ModelComponent.stockEntityList.isEmpty) {
//              TushareInitMA4ModelMA5ModelComponent.stockEntityList ++= this.stockMapper.select_MA4_MA5_create(dateStr).asScala.toList
//            }
//            val resList = TushareInitMA4ModelMA5ModelComponent.stockEntityList.filter(e => e.stockType.equals(ele.modClsName.toUpperCase) && e.createtime.equals(dateStr))
//            if (resList.size == 0) {
//              val entity = new StockEntity
//              entity.id = UUID.randomUUID().toString.replaceAll("-", "")
//              entity.stockCode = ele.ts_code
//              entity.stockType = ele.modClsName.toUpperCase
//              entity.name = tushareAllStocksCSVComponent.getTsStock(ele.ts_code).getOrElse(new TsStock).name
//              entity.createtime = dateStr
//              this.stockMapper.insert(entity)
//
//              log.info(s"\n处理队列元素：${JSONObject.toJSONString(ele)}")
//              TushareInitMA4ModelMA5ModelComponent.stockEntityList.clear()
//            }
//          }
//
//        }
//        catch {
//          case exception: Exception =>
//            exception.printStackTrace()
//            log.error(exception.getMessage)
//        }
//      }
//    })
  }

  /***
   * 添加推荐数据
   * @param list
   */
  def add_MA4_MA5_MODEL(stockResultList: List[StockResultJson]): Unit = synchronized {

    try {
      val ma4ma5List = stockResultList.filter(e=>List(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR, TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR).contains(e.modClsName.toUpperCase))
      ma4ma5List.foreach(stockResult=>{
        val stockCode= stockResult.ts_code
        val stockType = stockResult.modClsName.toUpperCase
        val dateStr = stockResult.fileName.substring(0, 8)

        if(TushareInitMA4ModelMA5ModelComponent.stockEntityList.get(dateStr)==null){
          TushareInitMA4ModelMA5ModelComponent.stockEntityList.put(dateStr, this.stockMapper.select_MA4_MA5_create(dateStr))
        }
        val list = TushareInitMA4ModelMA5ModelComponent.stockEntityList.get(dateStr).asScala
          .filter(entity=>{
            entity.stockCode.equals(stockCode) && entity.stockType.equals(stockType)
          }).toList

        if(list.size==0 && this.stockMapper.select_MA4_MA5_create(dateStr).asScala.filter(e=>e.stockCode.equals(stockCode) && e.stockType.equals(stockType) && e.createtime.equals(dateStr)).size == 0){
          val entity = new StockEntity
          entity.id = UUID.randomUUID().toString.replaceAll("-", "")
          entity.stockCode = stockCode
          entity.stockType = stockType
          entity.name = tushareAllStocksCSVComponent.getTsStock(stockCode).getOrElse(new TsStock).name
          entity.createtime = dateStr
          this.stockMapper.insert(entity)
          //更新缓存
          log.info(s"写入推荐MA4,MA5数据：${JSONObject.toJSONString(entity, Feature.LargeObject)}")
          TushareInitMA4ModelMA5ModelComponent.stockEntityList.put(dateStr, this.stockMapper.select_MA4_MA5_create(dateStr))
        }
      })

    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        log.error(exception.getMessage)
    }
  }

  /***
   * 初始化历史 MA4_MODEL 和 MA5_MODEL 模型生成的历史数据
   */
  def init_MODEL_BACK_TEST_RESULT(): Unit = synchronized {
    val file = new File("D:\\development\\github\\llmweb\\web\\MODEL_BACK_TEST_RESULT.txt")
    if(!file.exists()){
      log.info(s"${file.getAbsolutePath} 不存在")
      return
    }
    val lines = FileUtils.readLines(file, "UTF-8")
    val modelSet = List(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR, TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR).map(_.toUpperCase).toSet
    val allEntitys = this.stockMapper.selectAll().asScala
      .filter(e=>modelSet.contains(e.stockType.toUpperCase))

    lines.asScala.foreach(line=>{
      println(s"MODEL_BACK_TEST_RESULT.line:${line}")
      val arr = line.split(",").toList
      if(arr.size>4 && modelSet.contains(arr(0).toUpperCase)){
        val stockType = arr(0)
        val stockCode = arr(1)
        val name = arr(2)
        val time = arr(4).replaceAll("【买入】", "")
        println(s"${stockType}, ${stockCode}, ${name}, ${time}")

        if (allEntitys.filter(e => e.stockCode.equals(stockCode) && e.createtime.startsWith(time)).size == 0) {
          val stock = new StockEntity
          stock.id = UUID.randomUUID().toString.replaceAll("-", "")
          stock.stockCode = stockCode.trim
          stock.name = name
          stock.stockType = stockType.toUpperCase
          stock.createtime = time
          stockMapper.insert(stock)
        }
        else {
          println("已存在")
        }
      }
    })
  }

}
