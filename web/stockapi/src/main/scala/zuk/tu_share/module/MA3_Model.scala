package zuk.tu_share.module

import zuk.tu_share.dto.{ModuleDay, TsStock}

import java.math.BigDecimal
import scala.collection.mutable.ListBuffer

class MA3_Model extends IModel {

  val stocks = new ListBuffer[String]()

  override def run(days: List[ModuleDay]): Unit = {
    if(days.size>=3){
      val list = days.take(3)
      val head = list.head
      if (
        list(0).ma.ma5.compareTo(list(0).ma.ma10) >= 0
          && list(1).ma.ma5.compareTo(list(1).ma.ma10) <= 0
          && list(2).ma.ma5.compareTo(list(2).ma.ma10) <= 0
          && new BigDecimal(head.turnover_rate).compareTo(BigDecimal(5)) >= 0 //换手率
          && new BigDecimal(head.turnover_rate).compareTo(BigDecimal(15)) <= 0 //换手率
          && new BigDecimal(head.change).compareTo(BigDecimal(4)) >= 0 //涨幅度
          && new BigDecimal(head.change).compareTo(BigDecimal(7)) <= 0 //涨幅度
      ) {
        stocks += head.ts_code
      }
    }
  }

  override def getTsStocks(): List[String] = stocks.toList
}
