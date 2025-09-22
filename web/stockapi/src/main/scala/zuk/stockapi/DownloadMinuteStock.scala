package zuk.stockapi

import java.util.concurrent.atomic.AtomicInteger

/***
 * 获取交易日分钟信息，在2点45分时执行
 */
object DownloadMinuteStock {

  def run(stockList: List[StockApiVo]) = {
    val num = new AtomicInteger(0)
    while (stockList.size != num.get()) {

    }
  }

}
