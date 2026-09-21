package zuk.tu_share.utils

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
import java.math.{BigDecimal, RoundingMode}
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.{ConcurrentHashMap, Executors}
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.math

object IncreateDecreateRateDescUtil {
  
  /***
   * 相比最高跌去多少，相比最低涨了多少
   * 
   * @param stockCode
   * @return (最近收盘价，较最近低位涨了多少，较最近最高位跌去多少，字符串描述)
   */
  def getDescription(stockCode: String): Option[(Float, Float, Float, String)] = {
    val list = Dataset_stock_dailydata_dir.getDailyDataList(stockCode)
    if(list!=null && list.size > 0) {
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
 

