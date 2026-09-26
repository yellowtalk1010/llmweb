package zuk.consolidation

// 文件名: ConsolidationScanner.scala
// 运行: scala ConsolidationScanner.scala

import zuk.similar.Bar
import zuk.tu_share.DataFrame

import java.math.RoundingMode
import java.util
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer
import scala.util.Random

case class Bar(
                date: String,
                open: Double,
                high: Double,
                low: Double,
                close: Double,
                volume: Double
              ) {
  var stockCode = ""
  var stockName = ""
  
}

object ConsolidationScanner {

  /** 最近 n 根 bar 的收盘价均值（均线） */
  def ma(bars: Seq[Bar], n: Int): Double = {
    if (bars.size < n) Double.NaN
    else bars.takeRight(n).map(_.close).sum / n
  }

  /** 前 offset 天的 n 日均线值（用于判断均线是否向上） */
  def maPrev(bars: Seq[Bar], n: Int, offset: Int): Double = {
    if (bars.size < n + offset) Double.NaN
    else {
      val end = bars.size - offset
      bars.slice(end - n, end).map(_.close).sum / n
    }
  }

  /**
   * 均线黏合：MA5/MA10/MA20 最大值/最小值 < 1.03
   */
  def isConsolidated(bars: Seq[Bar]): Boolean = {
    val ma5  = ma(bars, 5)
    val ma10 = ma(bars, 10)
    val ma20 = ma(bars, 20)

    if (Seq(ma5, ma10, ma20).exists(_.isNaN)) return false

    val max = math.max(ma5, math.max(ma10, ma20))
    val min = math.min(ma5, math.min(ma10, ma20))

    min != 0 && (max / min) < 1.03
  }

  /**
   * 均线向上：MA5 和 MA10 都比前一天高
   */
  def isTrendingUp(bars: Seq[Bar]): Boolean = {
    val ma5Today     = ma(bars, 5)
    val ma5Yesterday = maPrev(bars, 5, 1)
    val ma10Today    = ma(bars, 10)
    val ma10Yesterday = maPrev(bars, 10, 1)

    if (ma5Today.isNaN || ma5Yesterday.isNaN) return false

    ma5Today > ma5Yesterday && ma10Today > ma10Yesterday
  }

  /**
   * 小碎步：最近 5 天单日涨跌幅绝对值 < 3%，且阳线 >= 3 根
   */
  def isSmallSteps(bars: Seq[Bar]): Boolean = {
    if (bars.size < 6) return false

    val recent = bars.takeRight(5)
    var bullishDays = 0

    for (i <- recent.indices) {
      val curr = recent(i)
      val prev = if (i == 0) bars(bars.size - 6) else recent(i - 1)

      val change = (curr.close - prev.close) / prev.close
      if (math.abs(change) > 0.03) return false

      if (curr.close > curr.open) bullishDays += 1
    }

    bullishDays >= 3
  }

  /** 综合条件 */
  def isCandidate(bars: Seq[Bar]): Boolean =
    isConsolidated(bars) && isTrendingUp(bars) && isSmallSteps(bars)

  /** 生成模拟数据：前段震荡黏合，后段小碎步缓涨 */
  def genBars(days: Int, seed: Long): Seq[Bar] = {
    val rnd = new Random(seed)
    var price = 10.0
    val bars = scala.collection.mutable.ArrayBuffer[Bar]()

    for (i <- 1 to days) {
      val prevClose = price

      price =
        if (i <= 50) 10.0 + rnd.nextDouble() * 0.3          // 震荡区
        else prevClose * (1 + rnd.nextDouble() * 0.02 + 0.005) // 小碎步缓涨

      val open  = prevClose
      val close = price
      val high  = math.max(open, close) * (1 + rnd.nextDouble() * 0.01)
      val low   = math.min(open, close) * (1 - rnd.nextDouble() * 0.01)

      bars += Bar(f"2024-01-$i%02d", open, high, low, close, 1000000)
    }
    bars.toSeq
  }
  
 

  def main(args: Array[String]): Unit = {
    val bars = genBars(60, 42L)
    
    val list = new ListBuffer[Bar]
    
    val barsList = DataFrame.loadModelAnalysisDataSet.values.filter(_.size > 60).map(ls=>{
      val bars = ls.map(e=>{
        val bar = Bar(
          e.trade_date, 
          e.open.toDouble,
          e.high.toDouble,
          e.low.toDouble,
          e.close.toDouble,
          e.vol.toDouble
        )
        bar.stockCode = e.ts_code
        bar.stockName = e.name
        bar
      })
      bars.take(60).sortBy(_.date)
    })
    val count = new AtomicInteger(0)
    barsList.filter(! _.head.stockCode.contains("601827")).foreach(bars=>{
      for (i <- 20 to bars.size) {
        val window = bars.take(i)
        val consolidated = isConsolidated(window)
        val up = isTrendingUp(window)
        val small = isSmallSteps(window)

        val flag = if (consolidated && up && small) {
          list += bars(i-1)
          " ← 符合条件"
        } else {
          ""
        }
        val date = bars(i - 1).date
        println(f"${bars.head.stockCode}  ${bars.head.stockName}  $date  黏合=$consolidated  向上=$up  小碎步=$small$flag")
      }

      println(s"完成${count.incrementAndGet()}/${barsList.size}")
    })
    
    println("over")
    val map = new util.HashMap[String, String]()
    list.groupBy(_.stockCode).map(_._2.sortBy(_.date.toLong).reverse).filter(_.size>=10).foreach(ls=>{
      val stockCode = ls.head.stockCode
      val endDateMax = ls.head.date
      val startDateMin = ls.last.date
      val stockList = DataFrame.getDataForSelect(stockCode).filter(e=>e.trade_date.toLong <= endDateMax.toLong && e.trade_date.toLong >= startDateMin.toLong)
       
      import java.math.BigDecimal
      val frequency = new BigDecimal(ls.size).divide(new BigDecimal(stockList.size), 2, RoundingMode.DOWN).doubleValue()
      if(frequency > 0.5){
        val s = s"${ls.head.stockCode}  ${ls.head.stockName}  ${startDateMin}至${endDateMax} ${ls.size}/${stockList.size}=${frequency}" //计算时间跨度， 黏合频率
        //println(s)  
        map.put(frequency.toString, s)
      }
    })
    
    import scala.jdk.CollectionConverters.*
    map.asScala.toList.sortBy(_._1.toDouble).reverse.map(_._2).foreach(println)
    
    
//    val bars = genBars(60, 42L)
//
//    // 从第 20 天开始逐日判断
//    for (i <- 20 to bars.size) {
//      val window = bars.take(i)
//      val consolidated = isConsolidated(window)
//      val up           = isTrendingUp(window)
//      val small        = isSmallSteps(window)
//
//      val flag = if (consolidated && up && small) " ← 符合条件" else ""
//      val date = bars(i - 1).date
//      println(f"$date  黏合=$consolidated  向上=$up  小碎步=$small$flag")
//    }
  }
}