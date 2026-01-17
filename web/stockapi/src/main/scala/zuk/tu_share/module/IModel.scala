package zuk.tu_share.module

import zuk.tu_share.dto.{ModuleDay, TsStock}

import scala.collection.mutable.ListBuffer
import java.math.{BigDecimal, RoundingMode}

trait IModel {

  var sells = ListBuffer[ModuleDay]()
  var buy: ModuleDay = _

  def run(days: List[ModuleDay]): Unit //运行模型

  def getStockDto(): StockDto

  def desc():String

  def winRate: Float

  def reference: Float

  def filterPriceLimitUp(moduleDay: ModuleDay): Boolean = {
    if(moduleDay.priceLimit == null){
      true
    }
    else {
      //收盘价小于涨停价
      new BigDecimal(moduleDay.getClose).compareTo(moduleDay.priceLimit.priceLimitUp) < 0
    }
  }

  def limitUp(days: List[ModuleDay]): String = {
    try {
      val max = 30
      //历史上30天出现过涨停次数
      if (days.size > max) {
        val size = days.take(max).filter(_.change.toFloat >= 9.0).size
        s"${size}/30涨"
      }
      else {
        val size = days.filter(_.change.toFloat >= 9.0).size
        s"${size}/${days.size}涨"
      }
    }
    catch
      case exception: Exception => "0/0涨"
  }

  def limitDown(days: List[ModuleDay]): String = {
    try {
      val max = 30
      //历史上30天出现过涨停次数
      if (days.size > max) {
        val size = days.take(max).filter(_.change.toFloat <= -9.0).size
        s"${size}/30跌"
      }
      else {
        val size = days.filter(_.change.toFloat <= -9.0).size
        s"${size}/${days.size}跌"
      }
    }
    catch
      case exception: Exception => "0/0跌次"
  }

  def changeUpRate(days: List[ModuleDay]): Float = {
    try{
      //换手率超过5%的比例
      if(days.size==0){
        return 0.0
      }
      val max = 30
      var size = 0
        if (days.size > max) {
          size = days.take(max).filter(_.turnover_rate.toFloat >= 5.0).size
          new BigDecimal(size).divide(new BigDecimal(max), 1, RoundingMode.UP).floatValue()
      }
      else {
        size = days.filter(_.change.toFloat >= 5.0).size
        new BigDecimal(size).divide(new BigDecimal(days.size), 1, RoundingMode.UP).floatValue()
      }
    }
    catch
      case exception: Exception => 0.0
  }

}
