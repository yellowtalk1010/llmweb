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

import zuk.sast.spring.controller.component.*

class Dataset_stock_dailydata_dir {

  private val StockHistoryDailyDataMap = new ConcurrentHashMap[String, List[StockDailyData]]()
  private val StockRtkDataMap = new ConcurrentHashMap[String, StockDailyData]()

  /** *
   * 加载历史股票日线数据
   */
  private def load_history_stock_daily_data(): ConcurrentHashMap[String, List[StockDailyData]] = synchronized {
    try {
      if(StockHistoryDailyDataMap.size() > 5000){
        return StockHistoryDailyDataMap
      }
      
      Dataset_all_stocks_csv_file.load.map(_.ts_code).toSet
        .foreach(stockCode=>{
          val filename = stockCode.replaceAll("\\.", "_") + ".csv"
          val path = ParseCammandParam.param.engineInfo.stock_module_dir + File.separator + filename
          val file = new File(path)
          if (file.isFile && file.exists()) {
            log.info(s"加载股票历史日线基本数据，路径:${file.getAbsolutePath}")
            val list = loadAllStocks(file)
            StockHistoryDailyDataMap.put(stockCode, list)
          }
          else {
            log.error(s"不存在加载股票基本数据路径:${file.getAbsolutePath}")
          }
        })

      return StockHistoryDailyDataMap
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        return StockHistoryDailyDataMap
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

          stockDailyData.pre_close = record.get("pre_close")
          stockDailyData.open = record.get("open")
          stockDailyData.high = record.get("high")
          stockDailyData.low = record.get("low")
          stockDailyData.close = record.get("close")
          if(record.isMapped("change")){
            stockDailyData.change = record.get("change")
          }
          stockDailyData
        })
        .toList
      in.close()
      dataList
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        List.empty
    }
  }

}
