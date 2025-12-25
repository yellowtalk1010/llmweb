package zuk.tu_share.module

import zuk.tu_share.dto.ModuleDay

import java.math
import java.math.BigDecimal
import scala.collection.mutable.ListBuffer

/***
 * 反包，下跌
 */
class MA3_2_Model extends IModel {

  val stocks = ListBuffer[String]()

  override def run(days: List[ModuleDay]): Unit = {
    if(days.size>=5){
      val list = days.take(5)
      val head = list.head
      if (
        new BigDecimal(list(3).change).compareTo(new BigDecimal(0)) < 0       //下跌
          && new BigDecimal(list(2).change).compareTo(new BigDecimal(0)) < 0  //下跌
          && new BigDecimal(list(1).change).compareTo(new BigDecimal(0)) > 0  //上涨
          && new BigDecimal(list(0).change).compareTo(new BigDecimal(0)) < 0  //下跌

//          && List(list(1).ma.ma10.floatValue(), list(1).ma.ma20.floatValue(), list(1).ma.ma30.floatValue()).min < list(1).ma.ma5.floatValue()  //站上5日线条件

        && new BigDecimal(list(2).close).compareTo(new BigDecimal(list(3).close)) < 0 //连续2天下跌是递减趋势
        && new BigDecimal(list(1).low).compareTo(new BigDecimal(list(2).close)) < 0   //

        && new BigDecimal(list(1).vol).compareTo(new BigDecimal(list(2).vol).add(new BigDecimal(list(3).vol))) > 0   //上涨并且，反包两日下跌交易量
        && new BigDecimal(list(1).change).compareTo(new BigDecimal(4)) > 0
        && new BigDecimal(list(1).change).compareTo(new BigDecimal(9)) < 0
        && new BigDecimal(list(1).turnover_rate).compareTo(new BigDecimal(5)) > 0

        && new BigDecimal(list(1).high).compareTo(new BigDecimal(list(0).high)) > 0 //  最新一日的最高价，不超过上一个最高价，防止抛售（如果是均价是最好的）

        && List(list(3).low.toFloat,list(2).low.toFloat,list(1).low.toFloat).sorted.head < list(0).low.toFloat
        //进入最低价不能跌破上3日最低价
        

      ) {
        //缩量上涨
        stocks += head.ts_code
      }
    }
  }

  override def getTsStocks(): List[String] = stocks.toList

  override def desc(): String = "反包两日阴线后继续下跌"

  override def winRate: Float = 0.8980

  override def reference: Float = 0.00

}
