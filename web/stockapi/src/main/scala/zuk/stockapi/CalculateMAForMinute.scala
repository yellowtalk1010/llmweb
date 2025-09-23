package zuk.stockapi

import org.apache.commons.io.FileUtils

import java.io.File
import java.math
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer
import java.math.BigDecimal

object CalculateMAForMinute {

  def run(stocks: List[StockApiVo]): List[(StockApiVo, List[StockMaVo])] = {
    var list = ListBuffer[(StockApiVo, List[StockMaVo])]()
    val counter = new AtomicInteger(0)
    stocks.map(stock=>{
      try{
        val minuteStock = createNewStockDayVo(stock)
        if(minuteStock!=null){
          val sorted = CalculateMAForDay.getStockDayVos(stock)

          val preDayClose = sorted.head.getClose //上一个交易日的收盘价
          val changeRadio = (new BigDecimal(minuteStock.getClose).subtract(new BigDecimal(preDayClose))).divide(new BigDecimal(preDayClose), 5, math.BigDecimal.ROUND_UP)
          minuteStock.setChangeRatio((changeRadio.multiply(new BigDecimal(100))).toString) //涨跌幅

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

