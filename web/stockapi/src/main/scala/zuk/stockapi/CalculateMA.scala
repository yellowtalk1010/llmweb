package zuk.stockapi

import java.io.File
import java.util.concurrent.ConcurrentHashMap

object CalculateMA {

  var MAP = new ConcurrentHashMap[String, List[StockMaVo]]

  def run(stocks: List[StockApiVo]): Unit = {
    MAP.clear()
    stocks.foreach(stock=>{
      try{
        calCode(stock)
      }
      catch
        case exception: Exception => exception.printStackTrace()
    })
  }

  private def calCode(stock: StockApiVo): Unit = {
    val codeDataFile = new File(LoaderStockData.STOCK_DAY + File.separator + stock.getApi_code)
    println( s"${codeDataFile.getAbsolutePath}, ${codeDataFile.exists()}, ${stock.getName}")

  }

}

