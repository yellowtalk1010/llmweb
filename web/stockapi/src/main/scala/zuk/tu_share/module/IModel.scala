package zuk.tu_share.module

import zuk.tu_share.dto.{ModuleDay, TsStock}

import scala.collection.mutable.ListBuffer

trait IModel {

  var backTestTargetList = ListBuffer[ModuleDay]()

  def run(days: List[ModuleDay]): Unit //运行模型

  def getTsStocks(): List[String]

}
