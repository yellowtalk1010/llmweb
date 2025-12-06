package zuk.tu_share.module

import zuk.tu_share.dto.{ModuleDay, TsStock}

trait IModel {

  def run(days: List[ModuleDay]): Unit //运行模型

  def getTsStocks(): List[String]
}
