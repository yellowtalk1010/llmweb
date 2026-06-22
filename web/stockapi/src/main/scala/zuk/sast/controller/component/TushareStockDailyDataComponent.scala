package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.csv.CSVFormat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
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
  val StockEntityMap = new ConcurrentHashMap[String, StockEntity]()
  val StockDailyDataMap = new ConcurrentHashMap[String, List[StockDailyData]]()
  private val DAY_NUM = 60 //过去6个交易日

  def getIncreateRate(stockCode: String): Option[(Float, Float, Float, String)] = {
    if(StockDailyDataMap.get(stockCode)!=null){
      val list = StockDailyDataMap.get(stockCode)
      val ls = if(list.size > DAY_NUM) list.take(DAY_NUM) else list
      val head = ls.head
      val lowest = ls.sortBy(_.close.toFloat).reverse.last //过去60个交易日最低价
      val highest = ls.sortBy(_.close.toFloat).last //过去60个交易日最高价
      val lowRate = new BigDecimal(head.close).divide(new BigDecimal(lowest.close), 2, RoundingMode.DOWN).toString
      val hightRate = new BigDecimal(head.close).divide(new BigDecimal(highest.close), 2, RoundingMode.DOWN).toString
      val str = s"【${head.close}】【较${lowest.trade_date}低位${lowRate}】【较${highest.trade_date}高位${hightRate}】"
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

  @Value("${stock.daily.data.path}")
  @BeanProperty
  private var stock_daily_data_path: String = null

  @Autowired
  private var stockMapper: StockMapper = null

  private val executor = Executors.newSingleThreadExecutor()

  @PostConstruct
  def init(): Unit = synchronized {

    val file = new File(stock_daily_data_path)
    if (!file.isDirectory || !file.exists()) {
      log.error(s"股票基本数据路径：${stock_daily_data_path}，路径不存在")
      System.exit(1)
    }

    executor.execute(()=>{
      while (true){
        refresh_MA4_MA5_Stockentity()
//        log.info(s"\n${TushareStockDailyDataComponent.StockEntityMap.asScala.map(e=>s"${e._2.stockCode},${e._2.name},${e._2.createtime}").mkString("\n")}")
        refresh_stock_daily_data()
        refresh_rtk()
        Thread.sleep(5000)
      }
    })
  }

  /***
   * 获取历史数据
   */
  private def refresh_MA4_MA5_Stockentity(): Unit = {
    try {
      val today = LocalDate.now()
      val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
      for (i <- 0 until 14) {
        val date = today.minusDays(i) //从数据库中获取过去7天（含今天）中ma4， ma5的股票
        val dateStr = date.format(dateFormat)
        val list = this.stockMapper.select_MA4_MA5_By_Createtime(dateStr)
//        println(s"${list.size()}")
        list.forEach(e => {
          TushareStockDailyDataComponent.StockEntityMap.put(e.stockCode, e)
        })
      }
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        log.error(exception.getMessage)
    }
  }

  /***
   * 更新实时数据
   */
  private def refresh_rtk(): Unit = {
    try {
      val path = this.stock_daily_data_path + File.separator + "rt_k" + File.separator + "rt_k.csv"
      val rtkFile = new File(path)
      if(rtkFile.exists() && rtkFile.isFile){
        log.info(s"实时股票基本数据路径:${rtkFile.getAbsolutePath}")
        val list = loadAllStocks(rtkFile)
        list.map(e=>{
          e.trade_date = new SimpleDateFormat("yyyyMMdd").format(new Date)
        })
        val map = new ConcurrentHashMap[String, StockDailyData]()
        list.foreach(e=>{
          map.put(e.ts_code, e)
        })

        TushareStockDailyDataComponent.StockDailyDataMap.asScala.map(_._1).foreach(stockCode=>{
          if(map.get(stockCode)!=null){
            val list = TushareStockDailyDataComponent.StockDailyDataMap.get(stockCode).toBuffer
            list.prepend(map.get(stockCode))
            TushareStockDailyDataComponent.StockDailyDataMap.put(stockCode, list.toList)
          }
        })

        TushareStockDailyDataComponent.StockDailyDataMap.asScala.foreach(e=>{
          val stockCode = e._1
          if(map.get(stockCode)!=null){
            val rtk = map.get(stockCode)
            e._2.toBuffer.prepend(rtk).toList
          }
        })

        println()

      }
      else {
        log.error(s"不存在实时股票基本数据路径:${rtkFile.getAbsolutePath}")
      }
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        log.error(exception.getMessage)
    }
  }


  private def refresh_stock_daily_data(): Unit = {
    try {
      TushareStockDailyDataComponent.StockEntityMap.asScala.map(_._2)
        .filter(e=>TushareStockDailyDataComponent.StockDailyDataMap.get(e.stockCode)==null)
        .foreach(e=>{
          val filename = e.stockCode.replaceAll("\\.", "_") + ".csv"
          val path = this.stock_daily_data_path + File.separator + "module" + File.separator + filename
          val file = new File(path)
          if(file.isFile && file.exists()){
            log.info(s"加载股票基本数据路径:${file.getAbsolutePath}")
            val list = loadAllStocks(file)
            TushareStockDailyDataComponent.StockDailyDataMap.put(e.stockCode, list)
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
