package zuk.sast.controller

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.TsStock

import scala.beans.BeanProperty

@Component
class AllStocksCSVUtil {

  private val log = LoggerFactory.getLogger(classOf[AllStocksCSVUtil])

  @Value("${stock.all_stocks.csv.path}")
  @BeanProperty
  private var all_stocks_csv_path: String = "all_stocks.csv"

  @PostConstruct
  def init(): Unit = {
    log.info(s"all_stocks.csv文件路径：${all_stocks_csv_path}")
    val list = initAllStocksCSV()
    log.info(s"初始化完成，总数：${list.size}")
  }

  def initAllStocksCSV(): List[TsStock] = {
    val list = DataFrame.loadAllStocks(all_stocks_csv_path)
    list
  }

}
