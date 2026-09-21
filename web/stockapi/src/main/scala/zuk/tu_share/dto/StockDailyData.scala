package zuk.tu_share.dto

import scala.beans.BeanProperty

class StockDailyData() {
  //股票代码
  @BeanProperty var ts_code: String = ""
  //股票名称
  @BeanProperty var name: String = ""
  //交易日期
  @BeanProperty var trade_date: String = ""
  //上一个交易日收盘价
  @BeanProperty var pre_close: String = ""
  //交易日开盘价
  @BeanProperty var open: String = ""
  //交易日最高价
  @BeanProperty var high: String = ""
  //交易日最低价
  @BeanProperty var low: String = ""
  //交易日收盘价
  @BeanProperty var close: String = ""
  //涨跌幅
  @BeanProperty var change: String = ""
  
}