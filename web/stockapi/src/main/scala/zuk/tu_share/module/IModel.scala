package zuk.tu_share.module

import zuk.tu_share.dto.{ModuleDay, TsStock}

import scala.collection.mutable.ListBuffer

trait IModel {

  var sells = ListBuffer[ModuleDay]()
  var buy: ModuleDay = _

  def run(days: List[ModuleDay]): Unit //运行模型

  def getTsStocks(): List[String]

  def desc():String

  def winRate: Float

  def reference: Float

  def limitUp(days: List[ModuleDay]): Boolean = {
    try {
      //历史上30天出现过涨停
      if (days.size > 30) {
        days.take(30).filter(_.change.toFloat >= 9.0).size > 0
      }
      else {
        days.filter(_.change.toFloat >= 9.0).size > 0
      }
    }
    catch
      case exception: Exception=> true
  }

}
