package zuk.sast.spring.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.TsStock
import zuk.tu_share.utils.All_stocks_csv_file_Util

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer

object TushareAllStocks {


  val allStocks = ListBuffer[TsStock]()

  initAllStocksCSV()

  def initAllStocksCSV(): List[TsStock] = synchronized {
    allStocks.clear()
    allStocks ++= All_stocks_csv_file_Util.load
    allStocks.toList
  }

  def getTsStock(tsCode: String): Option[TsStock] = {
    if(StringUtils.isEmpty(tsCode)){
      Option.empty
    }
    else {
      val ls = All_stocks_csv_file_Util.load.filter(_.ts_code.equals(tsCode))
      if(ls.size > 0){
        Some(ls.head)
      }
      else {
        Option.empty
      }
    }
  }

  def getAll(): List[TsStock] = {
    All_stocks_csv_file_Util.load
  }

}

/***
 * 自动化加载 all_stocks.csv 数据
 */
@Component
class TushareAllStocksCSVComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareAllStocksCSVComponent])

  @Autowired
  private var applicationProperties: ApplicationProperties = null

  @PostConstruct
  def init(): Unit = synchronized {

    val all_stocks_csv_path = applicationProperties.getStockAnalysisSystem_allStocksCsvPath

    if(TushareAllStocks.allStocks.size < 5000){
      log.info("")
      val list = TushareAllStocks.initAllStocksCSV()
      TushareAllStocks.allStocks.clear()
      TushareAllStocks.allStocks ++= list
    }

    log.info(s"all_stocks.csv初始化完成，总数：${TushareAllStocks.allStocks.size}")
  }

//  def getTsStock(tsCode: String): Option[TsStock] = {
//    val ls = TushareAllStocks.allStocks.filter(_.ts_code.equals(tsCode))
//    if(ls.size>0){
//      return Some(ls.head)
//    }
//    Option.empty
//  }

//  def getAll(): List[TsStock] = {
//    TushareAllStocks.allStocks.toList
//  }

}
