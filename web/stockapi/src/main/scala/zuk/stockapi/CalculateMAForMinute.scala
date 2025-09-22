package zuk.stockapi

import org.apache.commons.io.FileUtils

import java.io.File
import java.math
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer

object CalculateMAForMinute {

  def run(stocks: List[StockApiVo]): List[(StockApiVo, List[StockMaVo])] = {
    var list = ListBuffer[(StockApiVo, List[StockMaVo])]()
    val counter = new AtomicInteger(0)
    stocks.map(stock=>{
      try{
        val minuteStock = createNewStockDayVo(stock)
        if(minuteStock!=null){
          val sorted = CalculateMAForDay.getStockDayVos(stock)
          val malist = CalculateMAForDay.calStockMA(List(minuteStock) ++ sorted)
          (stock, malist)
        }
        else {
          (stock, List())
        }
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

  def createNewStockDayVo(stock: StockApiVo): StockDayVo = {
    val stockMinuteVoList = DownloadMinuteStock.getStockMinuteVos(stock.getApi_code)
    if(stockMinuteVoList!=null && stockMinuteVoList.size>0){
      val stockMinuteVo = stockMinuteVoList.last
      val stockDayVo = new StockDayVo
      stockDayVo.setClose(stockMinuteVo.getPrice)
      return stockDayVo
    }
    null
  }

}

