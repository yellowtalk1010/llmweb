package zuk.stockapi

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.io.FileUtils

import java.io.File
import java.math
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object CalculateMAForDay {

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
//    val codeDataFile = new File(LoaderLocalStockData.STOCK_DAY + File.separator + code)
//    println( s"${codeDataFile.getAbsolutePath}, ${codeDataFile.exists()}, ${stock.getName}")
    val formatter = DateTimeFormatter.ofPattern("yyyyMM")
    val today = LocalDate.now
    val num = new AtomicInteger(0)
    val stockDayVoList = new ListBuffer[StockDayVo]
    for (i <- 0 until 3) {
      val premonthDate = today.minusMonths(i)
      val preMonth = formatter.format(premonthDate)
      val path = LoaderLocalStockData.STOCK_DAY + File.separator + code + File.separator + preMonth + ".jsonl"
      val file = new File(path)
      if (!file.exists) {
        println(path + "，不存在")
      }
      else {
        println(path + "，存在")
        try {
          //读取文件中的数据
          val lines = FileUtils.readLines(file, "UTF-8")
          //将文件行数据转成json对象
          val ls: List[StockDayVo] = lines.asScala.map((line: String) => {
            val stockDayVo = JSONObject.parseObject(line, classOf[StockDayVo])
            stockDayVo
          }).toList
          stockDayVoList ++= ls
        }
        catch
          case exception: Exception => exception.printStackTrace()
      }
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

