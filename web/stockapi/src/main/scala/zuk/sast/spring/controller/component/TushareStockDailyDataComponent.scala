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
import zuk.tu_share.dto.TsStock

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

case class StockDailyData() {
  @BeanProperty var ts_code: String = ""
  @BeanProperty var name: String = ""
  @BeanProperty var trade_date: String = ""
  @BeanProperty var open: String = ""
  @BeanProperty var high: String = ""
  @BeanProperty var low: String = ""
  @BeanProperty var close: String = ""
}

object TushareStockDailyDataComponent {

  private val StockHistoryDailyDataMap = new ConcurrentHashMap[String, List[StockDailyData]]()
  private val StockRtkDataMap = new ConcurrentHashMap[String, StockDailyData]()
  private val DAY_NUM = 120 //过去6个交易日

  /***
   *
   * @param stockCode
   * @return (最近收盘价，较最近低位涨了多少，较最近最高位跌去多少，字符串描述)
   */
  def getIncreateRate(stockCode: String): Option[(Float, Float, Float, String)] = {
    val list = StockHistoryDailyDataMap.get(stockCode)
    if(list!=null){
      if(StockRtkDataMap.get(stockCode)!=null){
        list.toBuffer.prepend(StockRtkDataMap.get(stockCode))
      }

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
/***
 * 加载股票基本数据
 */
@Component
class TushareStockDailyDataComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareStockDailyDataComponent])

  @Autowired
  private var applicationProperties: ApplicationProperties = null

  @Autowired
  private var stockMapper: StockMapper = null

  private val executor = Executors.newSingleThreadExecutor()

  @PostConstruct
  def init(): Unit = synchronized {
    executor.execute(()=>{
      while (true){
        refresh_stock_daily_data()
        refresh_rtk()
        Thread.sleep(5000)
      }
    })
  }

  /***
   * 更新实时数据
   */
  private def refresh_rtk(): Unit = {
    try {
      val path = applicationProperties.getStockAnalysisSystem_rtkPath
      val rtkFile = new File(path)
      if(rtkFile.exists() && rtkFile.isFile){
        log.info(s"实时股票基本数据路径:${rtkFile.getAbsolutePath}")
        val list = loadAllStocks(rtkFile).filter(e=>{
          //过滤掉停牌的数据
          val isTingPai = new BigDecimal(e.close).compareTo(java.math.BigDecimal.ZERO) == 0
            || new BigDecimal(e.low).compareTo(java.math.BigDecimal.ZERO) == 0
            || new BigDecimal(e.high).compareTo(java.math.BigDecimal.ZERO) == 0
            || new BigDecimal(e.open).compareTo(java.math.BigDecimal.ZERO) == 0
          !isTingPai
        })
        val dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date)
        list.map(e=>{
          e.trade_date = dateStr
          TushareStockDailyDataComponent.StockRtkDataMap.put(e.ts_code, e)
        })

      }
      else {
        //log.error(s"不存在实时股票基本数据路径:${rtkFile.getAbsolutePath}")
      }
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        log.error(exception.getMessage)
    }
  }


  /***
   * 加载历史股票日线数据
   */
  private def refresh_stock_daily_data(): Unit = {
    try {
      TushareInitMA4ModelMA5ModelComponent.getStockEntityList.map(_.stockCode).toSet
        .filter(e=>TushareStockDailyDataComponent.StockHistoryDailyDataMap.get(e)==null)
        .foreach(stockCode=>{
          val filename = stockCode.replaceAll("\\.", "_") + ".csv"
          val stock_daily_data_path: String = applicationProperties.getStockAnalysisSystemPath
          val path = stock_daily_data_path + File.separator + "module" + File.separator + filename
          val file = new File(path)
          if(file.isFile && file.exists()){
            log.info(s"加载股票基本数据路径:${file.getAbsolutePath}")
            val list = loadAllStocks(file)
            TushareStockDailyDataComponent.StockHistoryDailyDataMap.put(stockCode, list)
          }
          else {
            log.error(s"不存在加载股票基本数据路径:${file.getAbsolutePath}")
          }
        })
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        log.error(exception.getMessage)
    }
  }

  private def loadAllStocks(all_stocks_file: File): List[StockDailyData] = {

    try {

      //将tushare的csv数据转成对象
      val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

      val dataList = records.asScala.map(record => {
          val stockDailyData = new StockDailyData()
          stockDailyData.ts_code = record.get("ts_code")
          stockDailyData.name = record.get("name")
          if(record.isMapped("trade_date")){
            stockDailyData.trade_date = record.get("trade_date")
          }
          else {
            stockDailyData.trade_date = ""
          }

          stockDailyData.open = record.get("open")
          stockDailyData.high = record.get("high")
          stockDailyData.low = record.get("low")
          stockDailyData.close = record.get("close")
          stockDailyData
        })
        .toList
      in.close()
      dataList
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        log.error(exception.getMessage)
        List.empty
    }
  }

}
