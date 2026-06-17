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

  def getIncreateRate(stockCode: String): String = {
    if(StockDailyDataMap.get(stockCode)!=null){
      val list = StockDailyDataMap.get(stockCode)
      val ls = if(list.size > DAY_NUM) list.take(DAY_NUM) else list
      val head = ls.head
      val lowest = ls.slice(1, ls.size).sortBy(_.close.toFloat).reverse.last
      val rate = new BigDecimal(head.close).divide(new BigDecimal(lowest.close), 2, RoundingMode.DOWN).toString
      s"【较${lowest.trade_date}低位${rate}】"
    }
    else {
      ""
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
        log.info(s"\n${TushareStockDailyDataComponent.StockEntityMap.asScala.map(e=>s"${e._2.stockCode},${e._2.name},${e._2.createtime}").mkString("\n")}")
        refresh_stock_daily_data()
        Thread.sleep(5000)
      }
    })
  }

  private def refresh_MA4_MA5_Stockentity(): Unit = {
    try {
      log.info(s"股票基本数据路径：${stock_daily_data_path}")
      val today = LocalDate.now()
      val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
      for (i <- 0 until 14) {
        val date = today.minusDays(i) //从今天开始往回测7天
        val dateStr = date.format(dateFormat)
        val list = this.stockMapper.select_MA4_MA5_By_Createtime(dateStr)
        println(s"${list.size()}")
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

  private def refresh_stock_daily_data(): Unit = {
    try {
      TushareStockDailyDataComponent.StockEntityMap.asScala.map(_._2)
        .filter(e=>TushareStockDailyDataComponent.StockDailyDataMap.get(e.stockCode)==null)
        .foreach(e=>{
          val filename = e.stockCode.replaceAll("\\.", "_") + ".csv"
          val path = this.stock_daily_data_path + File.separator + filename
          val file = new File(path)
          if(file.isFile && file.exists()){
            log.info(s"加载股票基本数据路径:${file.getAbsolutePath}")
            val list = loadAllStocks(file)
            TushareStockDailyDataComponent.StockDailyDataMap.put(e.stockCode, list)
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
          stockDailyData.trade_date = record.get("trade_date")
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
