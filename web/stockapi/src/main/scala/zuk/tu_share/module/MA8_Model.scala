package zuk.tu_share.module

import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.ModuleDay

import java.io.File
import java.nio.file.{Path, Paths}
import scala.collection.mutable.ListBuffer

object MA8_Model {

  def load(): Unit = {
    val topInstDirPath = Paths.get("D:\\development\\github\\tushare\\111\\tushare\\hm\\top_inst\\")
    println(s"龙虎榜路径:${topInstDirPath.toFile.getAbsoluteFile}, ${topInstDirPath.toFile.exists()}")
    val files = new ListBuffer[File]
    topInstDirPath.toFile.listFiles().toList.sortBy(e=>e.getName).reverse.foreach(yearDir=>{
      for(f <- yearDir.listFiles().sortBy(_.getName).reverse if files.size<=200) {
        files += f
      }
    })
    files.map(_.getName).foreach(println)
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

  override def run(days: List[ModuleDay]): Unit = ???

  override def getStockDto(): StockDto = ???

  override def desc(): String = ???

  override def winRate: Float = ???

  override def reference: Float = ???

  override def warnUpperShadow: Boolean = ???
}
