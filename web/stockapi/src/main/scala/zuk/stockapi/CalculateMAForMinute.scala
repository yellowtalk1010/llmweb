package zuk.stockapi

import java.math
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer

object CalculateMAForMinute {

  def run(stocks: List[StockApiVo]): List[(StockApiVo, List[StockMaVo])] = {
    var list = ListBuffer[(StockApiVo, List[StockMaVo])]()
    val counter = new AtomicInteger(0)
    stocks.map(stock=>{
      try{
        val sorted = CalculateMAForDay.getStockDayVos(stock)
        val malist = CalculateMAForDay.calStockMA(sorted)
        (stock, malist)
      }
      catch {
        case exception: Exception =>
          exception.printStackTrace()
          (stock, List())
      }
      finally {
        counter.incrementAndGet()
        println(s"${counter.get()}/${stocks.size}")
      }
    })

  }

}

