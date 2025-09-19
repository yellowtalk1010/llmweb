package zuk.stockapi

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
    
  }

}

