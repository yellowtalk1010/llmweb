package zuk.sast.spring.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import zuk.sast.spring.controller.TushareStockController
import zuk.sast.spring.controller.mapper.StockMapper
import zuk.sast.spring.controller.mapper.entity.StockEntity
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.TsStock
import zuk.tu_share.utils.Dataset_stock_dailydata_dir

import java.io.{File, FileReader}
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.{ConcurrentHashMap, Executors}
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*
import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.math

//case class StockDailyData() {
//  //股票代码
//  @BeanProperty var ts_code: String = ""
//  //股票名称
//  @BeanProperty var name: String = ""
//  //交易日期
//  @BeanProperty var trade_date: String = ""
//  //上一个交易日收盘价
//  @BeanProperty var pre_close: String = ""
//  //交易日开盘价
//  @BeanProperty var open: String = ""
//  //交易日最高价
//  @BeanProperty var high: String = ""
//  //交易日最低价
//  @BeanProperty var low: String = ""
//  //交易日收盘价
//  @BeanProperty var close: String = ""
//  //涨跌幅
//  @BeanProperty var change: String = ""
//}

object TushareStockDailyDataComponent {
  
  /***
   * 相比最高跌去多少，相比最低涨了多少
   * 
   * @param stockCode
   * @return (最近收盘价，较最近低位涨了多少，较最近最高位跌去多少，字符串描述)
   */
  def getIncreateRateDescription(stockCode: String): Option[(Float, Float, Float, String)] = {
    val list = Dataset_stock_dailydata_dir.getDailyDataList(stockCode)
    if(list!=null){
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
 

