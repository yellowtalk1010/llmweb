package zuk.stockapi

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.csv.CSVFormat
import org.apache.commons.io.FileUtils
import zuk.run.RunModule3

import java.io.{File, FileReader}
import java.math
import java.math.BigDecimal
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object CalculateMAForDay_Tushare {

  def run(stocks: List[StockApiVo]): List[(StockApiVo, List[StockMaVo])] = {
    var list = ListBuffer[(StockApiVo, List[StockMaVo])]()
    val counter = new AtomicInteger(0)
    stocks.map(stock=>{
      try{
        val sorted = getStockDayVos(stock)
        val malist = calStockMA(sorted)
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

  /**
   * 计算MA和AVG
   */
  def calStockMA(sorted: List[StockDayVo]): List[StockMaVo] = {

    val stockMaVoList = ListBuffer[StockMaVo]()
    for (index <- 0 until sorted.size - 31) {

      val subList = sorted.slice(index, index + 30)
      val stockDayVo = subList.head
      val time = stockDayVo.getTime

      val ma5 = this.cala_ma(subList.take(5))
      val ma10 = this.cala_ma(subList.take(10))
      val ma20 = this.cala_ma(subList.take(20))
      val ma30 = this.cala_ma(subList.take(30))

      val avg5 = this.cala_avg(subList.take(5))
      val avg10 = this.cala_avg(subList.take(10))
      val avg20 = this.cala_avg(subList.take(20))
      val avg30 = this.cala_avg(subList.take(30))

      val stockMaVo = new StockMaVo()
      stockMaVo.setTime(time)

      stockMaVo.setStockDayVo(stockDayVo)

      stockMaVo.setMa5(ma5.toString)
      stockMaVo.setMa10(ma10.toString)
      stockMaVo.setMa20(ma20.toString)
      stockMaVo.setMa30(ma30.toString)

      stockMaVo.setAvg5(avg5.toString)
      stockMaVo.setAvg10(avg10.toString)
      stockMaVo.setAvg20(avg20.toString)
      stockMaVo.setAvg30(avg30.toString)

      stockMaVoList += stockMaVo
    }

    stockMaVoList.toList

  }

  def getStockDayVos(stock: StockApiVo): List[StockDayVo] = {
    val code = stock.getApi_code
    val formatter = DateTimeFormatter.ofPattern("yyyyMM")
    val today = LocalDate.now
    val num = new AtomicInteger(0)
    val stockDayVoList = new ListBuffer[StockDayVo]

    val path = s"${RunModule3.PATH}\\module\\${stock.getApi_code}_${stock.getJys}.csv"
    val file = new File(path)
    if (!file.exists) {
      println(path + "，不存在")
    }
    else {
      println(path + "，存在")
      try {
        //读取文件中的数据
        val in = new FileReader(path, Charset.forName("UTF-8"))
        val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
        val ls: List[StockDayVo] = records.asScala.map(record=>{
          val ts_code = record.get("ts_code")
          val trade_date = record.get("trade_date")
          val open = record.get("open")
          val turnover_rate = record.get("turnover_rate")
          val amount = record.get("amount")
          val high = record.get("high")
          val low = record.get("low")
          val change = record.get("change")
          val close = record.get("close")
          val vol = record.get("vol")

          val stockDayVo = new StockDayVo()
          stockDayVo.setCode(ts_code)
          stockDayVo.setTime(trade_date)
          stockDayVo.setOpen(open)
          stockDayVo.setTurnoverRatio(turnover_rate)
          stockDayVo.setAmount(amount)
          stockDayVo.setHigh(high)
          stockDayVo.setLow(low)
          stockDayVo.setChangeRatio(change)
          stockDayVo.setClose(close)
          stockDayVo.setVolume(vol)

          stockDayVo
        }).toList
        in.close()
        stockDayVoList ++= ls
      }
      catch
        case exception: Exception => exception.printStackTrace()
    }

    //按时间降序
    val sorted = stockDayVoList.sortBy(_.getTime).reverse.toList
    sorted
  }

  private def cala_ma(stockDayVoList: List[StockDayVo]): BigDecimal = {
    //收盘价的平均值
    val optSum = stockDayVoList.map(e=>{
      new BigDecimal(e.getClose)
    }).reduceOption((a,b)=>a.add(b))
    val ma = optSum.get.divide(new BigDecimal(stockDayVoList.size),5, BigDecimal.ROUND_HALF_UP)
    ma
  }

  private def cala_avg(stockDayVoList: List[StockDayVo]): BigDecimal = {
    //日均价
    val volume = stockDayVoList.filter(_.getVolume!=null).map(e=>{new BigDecimal(e.getVolume)}).reduceOption((a,b)=>a.add(b)).get
    val amount = stockDayVoList.filter(_.getAmount!=null).map(e=>{new BigDecimal(e.getAmount)}).reduceOption((a,b)=>a.add(b)).get
    if(volume.compareTo(math.BigDecimal.ZERO)==0 || amount.compareTo(math.BigDecimal.ZERO) == 0){
      math.BigDecimal.ZERO
    }
    else {
      val avg = amount.divide(volume, 5, BigDecimal.ROUND_HALF_UP);
      avg
    }
  }

}

