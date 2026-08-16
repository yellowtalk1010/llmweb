package zuk.tu_share.module

import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.ModuleDay

import java.io.File
import java.nio.file.{Path, Paths}
import java.util
import scala.collection.mutable.ListBuffer

object MA8_Model {

  val topInstFiles = new ListBuffer[File]
//  val topInstMap = new util.HashMap[String, ]()

  load()

  def load(): Unit = synchronized {
    if(topInstFiles==null || topInstFiles.isEmpty){
      val topInstDirPath = Paths.get("D:\\development\\github\\tushare\\111\\tushare\\hm\\top_inst\\")
      println(s"龙虎榜路径:${topInstDirPath.toFile.getAbsoluteFile}, ${topInstDirPath.toFile.exists()}")

      topInstDirPath.toFile.listFiles().toList.sortBy(e=>e.getName).reverse.foreach(yearDir=>{
        for(f <- yearDir.listFiles().sortBy(_.getName).reverse if topInstFiles.size<=200) {
          topInstFiles += f
        }
      })
      topInstFiles.foreach(file=>{
        println(file.getName)

      })
    }
  }
}

class MA8_Model extends IModel {

  override def buyReason(): String = super.buyReason()

  override def backTestStep: Int = super.backTestStep

  override def filterPriceLimitUp(moduleDay: ModuleDay): Boolean = super.filterPriceLimitUp(moduleDay)

  override def limitUp(days: List[ModuleDay]): String = super.limitUp(days)

  override def limitDown(days: List[ModuleDay]): String = super.limitDown(days)

  override def changeUpRate(days: List[ModuleDay]): Float = super.changeUpRate(days)

  /** *
   * 上引线比例
   *
   * @param days
   * @return
   */
  override def upperShadow(days: List[ModuleDay]): Boolean = super.upperShadow(days)

  override def run(days: List[ModuleDay]): Unit = {
//    println(MA8_Model.topInstFiles.size)
    val head = days.head
  }

  override def getStockDto(): StockDto = {
    null
  }

  override def desc(): String = {
    ""
  }

  override def winRate: Float = {
    0.0
  }

  override def reference: Float = {
    0.0
  }

  override def warnUpperShadow: Boolean = {
    true
  }
}
