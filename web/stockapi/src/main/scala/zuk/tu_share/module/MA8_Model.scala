package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.{DataFrame, ParseCammandParam}
import zuk.tu_share.dto.{ModuleDay, TopInst}
import zuk.tu_share.utils.TopInstUtil

import java.io.File
import java.nio.file.{Path, Paths}
import java.util
import scala.collection.mutable.ListBuffer
import java.math.{BigDecimal, RoundingMode}
import scala.jdk.CollectionConverters.*



object MA8_Model {


  val topInstMap = new util.HashMap[String, List[TopInst]]()

  load()

  def createKey(topInst: TopInst): String = topInst.trade_date + "." + topInst.ts_code

  def load(): Unit = synchronized {
    if(topInstMap==null || topInstMap.isEmpty){
      val topInstDirPath = Paths.get("D:\\development\\github\\tushare\\111\\tushare\\hm\\top_inst\\")
//      println(s"龙虎榜路径:${topInstDirPath.toFile.getAbsoluteFile}, ${topInstDirPath.toFile.exists()}")
      val topInstFiles = new ListBuffer[File]
      topInstDirPath.toFile.listFiles().toList.sortBy(e=>e.getName).reverse.foreach(yearDir=>{
        for(f <- yearDir.listFiles().sortBy(_.getName).reverse if topInstFiles.size<=100) {
          topInstFiles += f
        }
      })

      val mapList = topInstFiles.flatMap(file=>{
//          println(file.getName)
          val topInstList = TopInstUtil.loadData(file)
          topInstList.asScala
        }).groupBy(e=>createKey(e)) //日期 + 股票代码
        .map(tp2=>(tp2._1, tp2._2.filter(e=>StringUtils.isNotBlank(e.sell) && StringUtils.isNotBlank(e.buy) && StringUtils.isNotBlank(e.net_buy))))
        .filter(_._2.size>0)
        .toList
        .sortBy(_._1)
        .reverse
        .map(ls=>{
          val head = ls._2.head
          val topInst = new TopInst
          topInst.ts_code = head.ts_code
          topInst.ts_name = head.ts_name
          topInst.trade_date = head.trade_date
          topInst.sell = ls._2.map(_.sell.toFloat).sum.toString
          topInst.buy = ls._2.map(_.buy.toFloat).sum.toString
          topInst.net_buy = ls._2.map(_.net_buy.toFloat).sum.toString
          topInst
        })
        .groupBy(_.ts_code)

      mapList.foreach(tp2 => {
        topInstMap.put(tp2._1, tp2._2)
      })

//      println(s"股票总数:${topInstMap.size}")

    }
  }
}

class MA8_Model extends IModel {

  var stockDto: StockDto = _

  override def buyReason(): String = super.buyReason()

  override def backTestStep: Int = 5

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
//    println(MA8_Model.topInstMap.size)
    val head = days.head
    val second = days(1)
    val topInstList = MA8_Model.topInstMap.get(second.ts_code)
    if(topInstList!=null && topInstList.size>0 && topInstList.head.trade_date.equals(second.trade_date)){
      println(s"${second.ts_code}, ${second.name}, ${second.trade_date}")
      val topInstHead = topInstList.head
      if(topInstHead.net_buy.toFloat<0){
        //如果上一个交易日，龙虎榜卖出为负
        val min = List(Math.abs(topInstHead.buy.toFloat), Math.abs(topInstHead.sell.toFloat)).min
        val divideValue = new BigDecimal(Math.abs(topInstHead.net_buy.toFloat)).divide(new BigDecimal(min), 4, RoundingMode.UP).floatValue()
        if(head.high.toFloat > second.close.toFloat
//          && head.change.toFloat > 0
//          && second.change.toFloat > 0
          && head.vol.toFloat < second.vol.toFloat
          && divideValue > 0.3
        ){
          val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
          stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
        }
      }
    }


  }

  override def getStockDto(): StockDto = {
    stockDto
  }

  override def desc(): String = {
    "龙虎榜净卖出为负，缩量上涨"
  }

  override def winRate: Float = {
    0.8308
  }

  override def reference: Float = {
    0.0
  }

  override def warnUpperShadow: Boolean = {
    true
  }
}
