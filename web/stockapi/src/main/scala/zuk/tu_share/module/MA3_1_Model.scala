package zuk.tu_share.module

import zuk.tu_share.dto.ModuleDay

import java.math.BigDecimal
import scala.collection.mutable.ListBuffer

class MA3_1_Model extends IModel {

  val stocks = ListBuffer[String]()

  override def run(days: List[ModuleDay]): Unit = {
    if(days.size>=4){
      val list = days.take(4)
      val head = list.head
      if (
        list(0).ma.ma5.compareTo(list(0).ma.ma10) >= 0
          && list(1).ma.ma5.compareTo(list(1).ma.ma10) <= 0
          && list(2).ma.ma5.compareTo(list(2).ma.ma10) <= 0
          && new BigDecimal(head.turnover_rate).compareTo(BigDecimal(0)) >= 0 //换手率
          && new BigDecimal(head.turnover_rate).compareTo(BigDecimal(5)) <= 0 //换手率
          && (new BigDecimal(list(1).turnover_rate).compareTo(BigDecimal(4)) > 0
            || new BigDecimal(list(2).turnover_rate).compareTo(BigDecimal(4)) > 0) //换手率
          && new BigDecimal(head.change).compareTo(BigDecimal(4)) >= 0 //涨幅度
          && new BigDecimal(head.change).compareTo(BigDecimal(9)) <= 0 //涨幅度
          && new BigDecimal(head.vol).compareTo(new BigDecimal(list(1).vol).add(new BigDecimal(list(2).vol))) < 0 //
          && new BigDecimal(head.high).compareTo(new BigDecimal(list(1).high)) > 0
      ) {
        //缩量上涨
        if(!head.name.contains("ST")){
          //ST不推荐
          stocks += head.ts_code
        }

      }
    }
  }

  override def getTsStocks(): List[String] = stocks.toList

  override def desc(): String = "【0.9200】，缩量上涨。过去60个交易日，"

}
