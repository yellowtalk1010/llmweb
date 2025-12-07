package zuk.tu_share.module

import zuk.tu_share.dto.ModuleDay

import java.math.BigDecimal
import scala.collection.mutable.ListBuffer

class MA3_2_Model extends IModel {

  val stocks = ListBuffer[String]()

  override def run(days: List[ModuleDay]): Unit = {
    if(days.size>=3){
      val list = days.take(3)
      val head = list.head
      if (
        new BigDecimal(list(2).change).compareTo(new BigDecimal(0)) < 0
        && new BigDecimal(list(1).change).compareTo(new BigDecimal(0)) < 0  //连续两天下跌
        && new BigDecimal(list(0).change).compareTo(new BigDecimal(0)) > 0

        && new BigDecimal(list(1).close).compareTo(new BigDecimal(list(2).close)) < 0 //连续2天下载
        && new BigDecimal(list(0).low).compareTo(new BigDecimal(list(1).close)) < 0

        && new BigDecimal(list(0).vol).compareTo(new BigDecimal(list(1).vol).add(new BigDecimal(list(2).vol))) > 0   //上涨并且，反包两日下跌交易量
        && new BigDecimal(list(0).change).compareTo(new BigDecimal(4)) > 0
        && new BigDecimal(list(0).change).compareTo(new BigDecimal(9)) < 0
        && new BigDecimal(list(0).turnover_rate).compareTo(new BigDecimal(5)) > 0

      ) {
        //缩量上涨
        stocks += head.ts_code
      }
    }
  }

  override def getTsStocks(): List[String] = stocks.toList

}
