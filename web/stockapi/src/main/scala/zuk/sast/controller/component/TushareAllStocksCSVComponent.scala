package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.TsStock

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer

object TushareAllStocks {

  val ALL_STOCKS_FILE: String = "all_stocks.csv"

  val allStocks = ListBuffer[TsStock]()

  private val allStockMap = new ConcurrentHashMap[String, TsStock]()

  initAllStocksCSV(ALL_STOCKS_FILE)

  def initAllStocksCSV(all_stocks_csv_path: String = ALL_STOCKS_FILE): List[TsStock] = synchronized {
    val file = new File(all_stocks_csv_path)
    println(s"TushareAllStocks在Object中加载全量股票数据文件:${file.getAbsolutePath},${file.exists()}")
    if(file.exists()){
      if(allStocks.size < 5000){
        val list = DataFrame.loadAllStocks(all_stocks_csv_path)
        allStocks.clear()
        allStocks ++= list
        allStocks.foreach(e=>{
          allStockMap.put(e.ts_code, e)
        })
      }
      allStocks.toList
    }
    else {
      List.empty
    }
  }

  def getTsStock(tsCode: String): Option[TsStock] = {
    val tsStock = allStockMap.get(tsCode)
    if (tsStock!=null) {
      Some(tsStock)
    }
    else {
      Option.empty
    }
  }

  def getAll(): List[TsStock] = {
    TushareAllStocks.allStocks.toList
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
      val list = TushareAllStocks.initAllStocksCSV(all_stocks_csv_path)
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
