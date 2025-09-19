package zuk.stockapi

import java.io.File
import java.util.concurrent.ConcurrentHashMap

object CalculateMA {

  var MAP = new ConcurrentHashMap[String, List[StockMaVo]]

  def run(codes: List[String]): Unit = {
    MAP.clear()
    codes.foreach(code=>{
      try{
        calCode(code)
      }
      catch
        case exception: Exception => exception.printStackTrace()
    })
  }

  private def calCode(code: String): Unit = {
    val codeDataFile = new File(LoaderStockData.STOCK_DAY + File.separator + code)
    println(codeDataFile.getAbsolutePath + s", ${codeDataFile.exists()}")
  }

}

