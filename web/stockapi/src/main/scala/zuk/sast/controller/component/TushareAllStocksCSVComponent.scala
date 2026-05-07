package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.TsStock

import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer

/***
 * 自动化加载 all_stocks.csv 数据
 */
@Component
class TushareAllStocksCSVComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareAllStocksCSVComponent])

  @Value("${stock.all_stocks.csv.path}")
  @BeanProperty
  private var all_stocks_csv_path: String = "all_stocks.csv"

  private val allStocks = ListBuffer[TsStock]()

  @PostConstruct
  def init(): Unit = synchronized {
    log.info(s"all_stocks.csv文件路径：${all_stocks_csv_path}")
    val list = initAllStocksCSV()
    if(allStocks.size < 5000){
      allStocks.clear()
      allStocks ++= list
    }

    log.info(s"初始化完成，总数：${list.size}")
  }

  def initAllStocksCSV(): List[TsStock] = {
    val list = DataFrame.loadAllStocks(all_stocks_csv_path)
    list
  }

  def getTsStock(tsCode: String): Option[TsStock] = {
    val ls = allStocks.filter(_.ts_code.equals(tsCode))
    if(ls.size>0){
      return Some(ls.head)
    }
    Option.empty
  }

}
