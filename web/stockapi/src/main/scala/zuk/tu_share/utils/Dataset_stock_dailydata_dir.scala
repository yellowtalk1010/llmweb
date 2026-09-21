package zuk.tu_share.utils

import org.apache.commons.csv.CSVFormat
import zuk.sast.spring.controller.component.*
import zuk.tu_share.ParseCammandParam

import java.io.{File, FileReader}
import java.math.{BigDecimal, RoundingMode}
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

import zuk.tu_share.dto.StockDailyData

object Dataset_stock_dailydata_dir {

  private val StockHistoryDailyDataMap = new ConcurrentHashMap[String, List[StockDailyData]]()
  private val StockRtkDataMap = new ConcurrentHashMap[String, StockDailyData]()
  
  /** *
   * 
   * @param stockCode
   * @return
   */
  def getDailyDataList(stockCode: String): List[StockDailyData] = {
    refresh_stock_daily_data()
    refresh_rtk()
    
    val list = new ListBuffer[StockDailyData]()
    if (StockHistoryDailyDataMap.get(stockCode) != null) {
      list ++= StockHistoryDailyDataMap.get(stockCode)
    }
    if (list != null) {
      if (StockRtkDataMap.get(stockCode) != null) {
        list.prepend(StockRtkDataMap.get(stockCode))
      }
    }
    list.toList
  }


  private def refresh_rtk(): Unit = {
    try {
      val path = ParseCammandParam.param.engineInfo.rtk_file
      val rtkFile = new File(path)
      if (rtkFile.exists() && rtkFile.isFile) {
        println(s"实时股票基本数据路径:${rtkFile.getAbsolutePath}")
        val list = loadAllStocks(rtkFile).filter(e => {
          try {
            //过滤掉停牌的数据
            val isTingPai = new BigDecimal(e.close).compareTo(java.math.BigDecimal.ZERO) == 0
              || new BigDecimal(e.low).compareTo(java.math.BigDecimal.ZERO) == 0
              || new BigDecimal(e.high).compareTo(java.math.BigDecimal.ZERO) == 0
              || new BigDecimal(e.open).compareTo(java.math.BigDecimal.ZERO) == 0
            !isTingPai
          }
          catch {
            case exception: Exception => false
          }
        })
        val dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date)
        list.map(e => {
          e.trade_date = dateStr
          e.change = new BigDecimal(e.close.toFloat - e.pre_close.toFloat).divide(new BigDecimal(e.pre_close), 4, RoundingMode.DOWN).multiply(new BigDecimal(100)).floatValue().toString
          StockRtkDataMap.put(e.ts_code, e)
        })

      }
      else {
        //log.error(s"不存在实时股票基本数据路径:${rtkFile.getAbsolutePath}")
      }
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
    }
  }
  

  /** *
   * 加载历史股票日线数据
   */
  private def refresh_stock_daily_data(): Unit = synchronized {
    try {
      if(StockHistoryDailyDataMap.size() > 5000){
        return
      }
      
      StockHistoryDailyDataMap.clear()
      
      Dataset_all_stocks_csv_file.load.map(_.ts_code).toSet
        .foreach(stockCode=>{
          val filename = stockCode.replaceAll("\\.", "_") + ".csv"
          val path = ParseCammandParam.param.engineInfo.stock_module_dir + File.separator + filename
          val file = new File(path)
          if (file.isFile && file.exists()) {
            println(s"加载股票历史日线基本数据，路径:${file.getAbsolutePath}")
            val list = loadAllStocks(file)
            StockHistoryDailyDataMap.put(stockCode, list)
          }
          else {
            println(s"不存在加载股票基本数据路径:${file.getAbsolutePath}")
          }
        })

    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
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
