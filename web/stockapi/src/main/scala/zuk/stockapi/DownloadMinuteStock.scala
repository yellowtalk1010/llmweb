package zuk.stockapi

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/***
 * 获取交易日分钟信息，在2点45分时执行
 */
object DownloadMinuteStock extends Download {

  def run(stockList: List[StockApiVo]) = {
    stockList.foreach(stockApiVO=>{
      try {
        val path = LoaderLocalStockData.STOCK_MINUTE + File.separator + stockApiVO.getApi_code + ".jsonl"
        val url = s"https://www.stockapi.com.cn/v1/base/min?token=${LoaderLocalStockData.TOKEN}&code=${stockApiVO.getApi_code}&all=1"
      }
      catch
        case exception: Exception =>
    })
  }

}
