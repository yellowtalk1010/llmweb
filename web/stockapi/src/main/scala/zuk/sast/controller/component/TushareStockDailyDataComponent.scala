package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.{ConcurrentHashMap, Executors}
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*


object TushareStockDailyDataComponent {
  val StockEntityList = new ConcurrentHashMap[String, StockEntity]()
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
      for (i <- 0 until 7) {
        val date = today.minusDays(i) //从今天开始往回测7天
        val dateStr = date.format(dateFormat)
        val list = this.stockMapper.select_MA4_MA5_By_Createtime(dateStr)
        list.forEach(e => {
          TushareStockDailyDataComponent.StockEntityList.put(e.stockCode, e)
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
      TushareStockDailyDataComponent.StockEntityList.asScala.map(_._2).foreach(e=>{

      })
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        log.error(exception.getMessage)
    }
  }

}
