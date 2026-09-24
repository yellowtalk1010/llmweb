package zuk.similar

import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, IncreateDecreateRateDescUtil}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Random
import scala.jdk.CollectionConverters.*
import java.math.{BigDecimal, RoundingMode}


// ============ 数据模型：完整K线 ============
case class Bar(
                date: String,
                open: Double,
                high: Double,
                low: Double,
                close: Double,
                volume: Double
              ) {
  
  //其他特征
  var feature: FeaturePoint = null
  
  /** 振幅：(high - low) / 昨收，这里用当日 open 近似，或用外部传入 */
  def amplitude: Double = new BigDecimal(if (open == 0) 0 else (high - low)).divide(new BigDecimal(open), 4, RoundingMode.UP).doubleValue()
  
  /** 实体幅度：(close - open) / open */
  def bodyRatio: Double = new BigDecimal(if (open == 0) 0 else (close - open)).divide(new BigDecimal(open), 4, RoundingMode.UP).doubleValue()

  /** 上影线比例 */
  def upperShadow: Double = {
    val top = math.max(open, close)
    new BigDecimal(if (open == 0) 0 else (high - top)).divide(new BigDecimal(open), 4, RoundingMode.UP).doubleValue()
  }

  /** 下影线比例 */
  def lowerShadow: Double = {
    val bottom = math.min(open, close)
    new BigDecimal(if (open == 0) 0 else (bottom - low)).divide(new BigDecimal(open), 4, RoundingMode.UP).doubleValue()
  }

  /** 是否阳线 */
  def isBullish: Boolean = close >= open
}

// ============ 特征点：可自由扩展维度 ============
case class FeaturePoint(
                         returnRate: Double,     // 涨跌幅 (今收-昨收)/昨收
                         volumeChange: Double,   // 成交量变化率
                         amplitude: Double,      // 振幅 (高-低)/开
                         bodyRatio: Double       // 实体 (收-开)/开
                       )
{
  var prevDate: String = ""
  var currDate: String = ""
}

case class SimilarResult(stockCode: String,
                         stockName: String,
                         startDate: String,
                         endDate: String,
                         distance: Double)

/***
 * 根据形态确定买、卖点
 */
object DTWStockSimilarity_B {

  /**
   * 提取特征序列
   * 注意：第 i 天的涨跌幅需要昨收，所以除了当日 Bar 还要传入前一日收盘价
   */
  def extractFeatures(prev: Bar, curr: Bar): Unit = {

    //  涨跌幅 (今收-昨收)/昨收
    //val ret = (curr.close - prev.close) / prev.close
    val ret = new BigDecimal(curr.close - prev.close).divide(new BigDecimal(prev.close), 4, RoundingMode.UP).doubleValue()
    //  成交量变化率
    //val volChg = (curr.volume - prev.volume) / prev.volume
    val volChg = new BigDecimal(curr.volume-prev.volume).divide(new BigDecimal(prev.volume), 4, RoundingMode.UP).doubleValue()

    val fp = FeaturePoint(ret, volChg, curr.amplitude, curr.bodyRatio)

    fp.prevDate = prev.date
    fp.currDate = curr.date

    curr.feature = fp

  }

  private def pointDist(a: FeaturePoint, b: FeaturePoint): Double = {
    val dr = a.returnRate - b.returnRate
    val dv = a.volumeChange - b.volumeChange
    val da = a.amplitude - b.amplitude
    val db = a.bodyRatio - b.bodyRatio

    //目前是4维，可以扩充到5,6,7,8维
    math.sqrt(dr * dr + dv * dv + da * da + db * db)
  }

  /** DTW 距离（四维特征，带 Sakoe-Chiba 窗口约束） */
  def dtwDistance(s1: Seq[FeaturePoint], s2: Seq[FeaturePoint]): Double = {
    val n = s1.size
    val m = s2.size
    val dp = Array.fill(n, m)(Double.MaxValue)
    dp(0)(0) = pointDist(s1.head, s2.head)

    for (i <- 1 until n; j <- math.max(1, i - 2) until math.min(m, i + 3)) {
      val cost = pointDist(s1(i), s2(j))
      val minPrev = math.min(
        math.min(dp(i - 1)(j), dp(i)(j - 1)),
        dp(i - 1)(j - 1)
      )
      dp(i)(j) = cost + minPrev
    }
    dp(n - 1)(m - 1)
  }
 

  /** 写法 B: 命令式 for 循环 */
  def findSimilarImperative(
                             target: Seq[Bar],
                             allStocks: Map[String, Seq[Bar]],
                             windowSize: Int
                           ): List[SimilarResult] = {

    val results = ListBuffer[SimilarResult]()

    for ((stock_code, bars) <- allStocks) {
      for (start <- 0 until bars.size - windowSize - 1) {

        val windowBars = bars.slice(start, start + windowSize)
        val dis = dtwDistance(target.map(_.feature), windowBars.map(_.feature))
        results += SimilarResult(
          stock_code,
          DataFrame.STOCKS_MAP.get(stock_code).name,
          windowBars.last.date,
          windowBars.head.date,
          new BigDecimal(dis).setScale(8, RoundingMode.UP).doubleValue())
      }
    }

    results.toList
  }

  def getTargetBars(stockCode: String): Seq[Bar] = {
    val ls = DataFrame.getDataForSelect(stockCode).map(e=>{
      val bar = Bar(
        e.trade_date,
        e.open.toDouble,
        e.high.toDouble,
        e.low.toDouble,
        e.close.toDouble,
        e.vol.toDouble
      )
      bar
    })

    for(i <- 0 until ls.size - 1) {
      val cur = ls(i)
      val prev = ls(i + 1)
      extractFeatures(prev, cur)
    }

    ls.slice(0, ls.size - 1)

  }

  def getAllBars(stockCode: String): Seq[Bar] = {
    if(DataFrame.getDataForSelect(stockCode)==null || DataFrame.getDataForSelect(stockCode).size == 0){
      return List.empty
    }
    val ls = DataFrame.getDataForSelect(stockCode).map(e=>{
      val bar = Bar(
        e.trade_date,
        e.open.toDouble,
        e.high.toDouble,
        e.low.toDouble,
        e.close.toDouble,
        e.vol.toDouble
      )
      bar
    })

    for (i <- 0 until ls.size - 1) {
      val cur = ls(i)
      val prev = ls(i + 1)
      extractFeatures(prev, cur)
    }

    ls.slice(0, ls.size - 1)
  }

  def main(args: Array[String]): Unit = {
    val windowSize = 5 //滑动的窗口

    // 1. 目标股票
    val tsCode = "000001.SZ"
    val targetBars = getTargetBars(tsCode).take(windowSize)
    val targetFeatures = targetBars.toList.map(_.feature)

    println("=== 目标形态（近5日特征）===")
    println("日期         涨跌幅    量变     振幅     实体")
    targetBars.toList.zipWithIndex.foreach {tp2 =>
      val bar = tp2._1
      val f = bar.feature
      val i = tp2._2
      println(f"${bar.date}  ${f.returnRate * 100}%6.2f%%  ${f.volumeChange * 100}%6.2f%%  ${f.amplitude * 100}%6.2f%%  ${f.bodyRatio * 100}%6.2f%%")
    }

    // 2. 模拟全市场

    val allStocks: Map[String, Seq[Bar]] = DataFrame.STOCKS_MAP.values().asScala
      .map(e=>{
        e.ts_code -> getAllBars(e.ts_code)
      }).toMap.filter(_._2.size>10)

    // 3. 两种写法 
    val t2 = System.nanoTime()
    val resB = findSimilarImperative(targetBars, allStocks, windowSize)
    val filterResB = resB.sortBy(_.distance).take(100)
    val t3 = System.nanoTime()


    println("=== 写法 B: 命令式 for 循环 ===")
    filterResB.foreach(res=>{
      val hits = ListBuffer[ModuleDay]()
      val ls = DataFrame.getDataForSelect(res.stockCode)
      for(i <- 0 until ls.size){
        if(ls(i).trade_date.equals(res.endDate)){
          var count = 0
          for(ii <- i to 0 by -1 if count < 3){
            count = count + 1
            hits += ls(ii)
          }
        }
      }

      val st = hits.filter(e=>e.high.toDouble > e.pre_close.toDouble 
        && e.change.toDouble > 1
      ).size > 0

      println(f"  ${res.stockCode}  截至 ${res.endDate}  距离=${res.distance}%.6f  成功=${st}") //相似度越小越相似

    })
    println(f"  耗时: ${(t3 - t2) / 1e6}%.2f ms\n")


  }

}