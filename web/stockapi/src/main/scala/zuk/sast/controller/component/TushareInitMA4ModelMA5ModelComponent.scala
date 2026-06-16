package zuk.sast.controller.component

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.controller.StockResultJson
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity

import java.io.File
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object TushareInitMA4ModelMA5ModelComponent {
  private val stockSet = new mutable.HashSet[String]
  val MA4_MODEL_STR: String = "MA4_MODEL"
  val MA5_MODEL_STR: String = "MA5_MODEL"
}

@Component
class TushareInitMA4ModelMA5ModelComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareInitMA4ModelMA5ModelComponent])

  @Autowired
  private var stockMapper: StockMapper = _


  def get_MD4_MODEL_LIST(): List[StockEntity] = synchronized {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR)).toList
  }

  def get_MD5_MODEL_LIST(): List[StockEntity] = synchronized {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR)).toList
  }

  /***
   * 添加推荐数据
   * @param list
   */
  def add_MA4_MA5_MODEL(list: List[StockResultJson]): Unit = synchronized {

    try {
      list.filter(_.modClsName.equals(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR)).map(e=>{

        if(!TushareInitMA4ModelMA5ModelComponent.stockSet.contains(e.ts_code)){
          TushareInitMA4ModelMA5ModelComponent.stockSet += e.ts_code

          val allEntitys = this.stockMapper.selectAll().asScala
            .filter(e=>List(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR, TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR).contains(e.stockType))

          val dateStr = e.fileName.substring(0, 8)
          if (allEntitys.filter(entity => entity.stockCode.equals(e.ts_code) && entity.createtime.startsWith(dateStr)).size == 0) {

            val entity = new StockEntity
            entity.id = UUID.randomUUID().toString.replaceAll("-", "")
            entity.stockCode = e.ts_code
            entity.stockType = TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR
            entity.name = e.name
            entity.createtime = dateStr

            this.stockMapper.insert(entity)
          }
        }

      })
    }
    catch {
      case exception: Exception => exception.printStackTrace()
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
