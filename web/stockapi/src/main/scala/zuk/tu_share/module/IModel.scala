package zuk.tu_share.module

import zuk.tu_share.dto.{ModuleDay, TsStock}

import scala.collection.mutable.ListBuffer
import java.math.{BigDecimal, RoundingMode}

trait IModel {

  var sells = ListBuffer[ModuleDay]()
  var buy: ModuleDay = _

  def run(days: List[ModuleDay]): Unit //运行模型

  def getStockDtos(): List[StockDto]

  def desc():String

  def winRate: Float

  def reference: Float

  def limitUp(days: List[ModuleDay]): Boolean = {
    try {
      val max = 30
      //历史上30天出现过涨停
      if (days.size > max) {
        days.take(max).filter(_.change.toFloat >= 9.0).size > 0
      }
      else {
        days.filter(_.change.toFloat >= 9.0).size > 0
      }
    }
    catch
      case exception: Exception=> true
  }

  def changeUpRate(days: List[ModuleDay]): Boolean = {
    try{
      if(days.size==0){
        return false
      }
      val size = days.filter(_.turnover_rate.toFloat >= 3.0).size
      new BigDecimal(size).divide(new BigDecimal(days.size), 5, RoundingMode.UP).compareTo(new BigDecimal(0.3)) > 0
    }
    catch
      case exception: Exception => false
  }

}
