package zuk.tu_share.utils

import zuk.tu_share.DataFrame

import java.math.{BigDecimal, RoundingMode}
import scala.math

object IncreateDecreateRateDescUtil {
  
  /***
   * 相比最高跌去多少，相比最低涨了多少
   * 
   * @param stockCode
   * @return (最近收盘价，较最近低位涨了多少，较最近最高位跌去多少，字符串描述)
   */
  def getDescription(stockCode: String): Option[(Float, Float, Float, String)] = {
    val map = DataFrame.loadModelAnalysisDataSet.get(stockCode)
    if(map.get!=null && !map.get.isEmpty && map.get.size > 0) {
      val list = map.get
      val DAY_NUM = 120 //过去6个交易日
      val ls = if(list.size > DAY_NUM) list.take(DAY_NUM) else list
      val head = ls.head
      val lowest = ls.sortBy(_.close.toFloat).reverse.last //过去60个交易日最低价
      val highest = ls.sortBy(_.close.toFloat).last //过去60个交易日最高价
      if(new java.math.BigDecimal(lowest.close).compareTo(java.math.BigDecimal.ZERO) == 0
        || new BigDecimal(highest.close).compareTo(java.math.BigDecimal.ZERO)==0){
        //可能是停牌
        Some((0,0,0,""))
      }
      val lowRate = new BigDecimal(head.close).divide(new BigDecimal(lowest.close), 2, RoundingMode.DOWN).toString
      val hightRate = new BigDecimal(head.close).divide(new BigDecimal(highest.close), 2, RoundingMode.DOWN).toString
      val str = s"【${head.close}】【较${lowest.trade_date}低位涨了${lowRate}】【较${highest.trade_date}高位跌去${hightRate}】"
      Some((head.close.toFloat, lowRate.toFloat, hightRate.toFloat, str))
    }
    else {
      Some((0,0,0,""))
    }
  }
  
  
}
 

