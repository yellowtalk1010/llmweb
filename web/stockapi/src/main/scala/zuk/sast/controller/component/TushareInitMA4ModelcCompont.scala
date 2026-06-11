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

object TushareInitMA4ModelcCompont {
  private val stockSet = new mutable.HashSet[String]
  private val MA4_MODEL_STR: String = "MA4_MODEL"
}

@Component
class TushareInitMA4ModelcCompont {

  private val log = LoggerFactory.getLogger(classOf[TushareInitMA4ModelcCompont])

  private val TIME: String = "101010"


  @Autowired
  private var stockMapper: StockMapper = _


  def get_MD4_MODEL_LIST(): List[StockEntity] = {
    this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelcCompont.MA4_MODEL_STR)).toList
  }

  /***
   * 添加推荐数据
   * @param list
   */
  def add_MA4_MODEL(list: List[StockResultJson]): Unit = {

    try {
      list.filter(_.modClsName.equals(TushareInitMA4ModelcCompont.MA4_MODEL_STR)).map(e=>{

        if(!TushareInitMA4ModelcCompont.stockSet.contains(e.ts_code)){
          TushareInitMA4ModelcCompont.stockSet += e.ts_code

          val allEntitys = this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelcCompont.MA4_MODEL_STR))

          val dateStr = e.fileName.substring(0, 8)
          if (allEntitys.filter(entity => entity.stockCode.equals(e.ts_code) && entity.createtime.startsWith(dateStr)).size == 0) {

            val entity = new StockEntity
            entity.id = UUID.randomUUID().toString.replaceAll("-", "")
            entity.stockCode = e.ts_code
            entity.stockType = TushareInitMA4ModelcCompont.MA4_MODEL_STR
            entity.name = e.name
            entity.createtime = dateStr + TIME

//            this.stockMapper.insert(entity)
          }
        }

      })
    }
    catch {
      case exception: Exception => exception.printStackTrace()
    }
  }

  /***
   * 初始化历史 MA4_MODEL 模型生成的历史数据
   */
  def init_MA4_MODEL_HISTORY(): Unit = {
    val file = new File("D:\\development\\github\\llmweb\\web\\MA4_MODEL.txt")
    if(!file.exists()){
      log.info(s"${file.getAbsolutePath} 不存在")
      return
    }
    val lines = FileUtils.readLines(file, "UTF-8")
    val allEntitys = this.stockMapper.selectAll().asScala.filter(_.stockType.equals(TushareInitMA4ModelcCompont.MA4_MODEL_STR))
    lines.asScala.foreach(line=>{
      val arr = line.split(",").toList
      val stockType = arr(0)
      val stockCode = arr(1)
      val name = arr(2)
      val time = arr(4).replaceAll("【买入】", "")
      println(s"${stockType}, ${stockCode}, ${name}, ${time}")

      if(allEntitys.filter(e=>e.stockCode.equals(stockCode) && e.createtime.startsWith(time)).size == 0){
        val stock = new StockEntity
        stock.id = UUID.randomUUID().toString.replaceAll("-", "")
        stock.stockCode = stockCode.trim
        stock.name = name
        stock.stockType = "MA4_MODEL"
        stock.createtime = time + TIME
        stockMapper.insert(stock)
      }
      else {
        println("已存在")
      }
    })
  }

}
