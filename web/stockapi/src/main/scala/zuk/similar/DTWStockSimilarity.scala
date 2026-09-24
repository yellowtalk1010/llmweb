//package zuk.similar
//
//import zuk.tu_share.DataFrame
//import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, IncreateDecreateRateDescUtil}
//
//import scala.collection.mutable
//import scala.collection.mutable.ArrayBuffer
//import scala.util.Random
//import scala.jdk.CollectionConverters.*
//import java.math.{BigDecimal, RoundingMode}
//
//
//case class Feature(prev: Bar, curr: Bar) {
//
//  //  涨跌幅 (今收-昨收)/昨收
//  val ret = new BigDecimal(curr.close - prev.close).divide(new BigDecimal(prev.close), 4, RoundingMode.UP).doubleValue()
//  
//  //  成交量变化率
//  val volChg = new BigDecimal(curr.volume - prev.volume).divide(new BigDecimal(prev.volume), 4, RoundingMode.UP).doubleValue()
//}  
//
//// ============ 数据模型：完整K线 ============
//case class Bar(
//                date: String,
//                open: Double,
//                high: Double,
//                low: Double,
//                close: Double,
//                volume: Double
//              ) {
//  
//  //其他特征
//  var other_feature: Feature = null
//  
//  /** 振幅：(high - low) / 昨收，这里用当日 open 近似，或用外部传入 */
//  def amplitude: Double = new BigDecimal(if (open == 0) 0 else (high - low)).divide(new BigDecimal(open), 4, RoundingMode.UP).doubleValue()
//  
//  /** 实体幅度：(close - open) / open */
//  def bodyRatio: Double = new BigDecimal(if (open == 0) 0 else (close - open)).divide(new BigDecimal(open), 4, RoundingMode.UP).doubleValue()
//
//  /** 上影线比例 */
//  def upperShadow: Double = {
//    val top = math.max(open, close)
//    new BigDecimal(if (open == 0) 0 else (high - top)).divide(new BigDecimal(open), 4, RoundingMode.UP).doubleValue()
//  }
//
//  /** 下影线比例 */
//  def lowerShadow: Double = {
//    val bottom = math.min(open, close)
//    new BigDecimal(if (open == 0) 0 else (bottom - low)).divide(new BigDecimal(open), 4, RoundingMode.UP).doubleValue()
//  }
//
//  /** 是否阳线 */
//  def isBullish: Boolean = close >= open
//}
//
//// ============ 特征点：可自由扩展维度 ============
//case class FeaturePoint(
//                         returnRate: Double,     // 涨跌幅 (今收-昨收)/昨收
//                         volumeChange: Double,   // 成交量变化率
//                         amplitude: Double,      // 振幅 (高-低)/开
//                         bodyRatio: Double       // 实体 (收-开)/开
//                       )
//
//case class SimilarResult(stockCode: String, endDate: String, distance: Double)
//
///***
// * 根据形态确定买、卖点
// */
//object DTWStockSimilarity {
//
//  /**
//   * 提取特征序列
//   * 注意：第 i 天的涨跌幅需要昨收，所以除了当日 Bar 还要传入前一日收盘价
//   */
//  def extractFeatures(bars: Seq[Bar], windowSize: Int): Seq[FeaturePoint] = {
//    if (bars.size < windowSize + 1){
//      Seq.empty
//    }
//    else {
//      (1 to windowSize).map { i =>
//        val prev = bars(i - 1)
//        val curr = bars(i)
//        //  涨跌幅 (今收-昨收)/昨收
//        //val ret = (curr.close - prev.close) / prev.close
//        val ret = new BigDecimal(curr.close - prev.close).divide(new BigDecimal(prev.close), 4, RoundingMode.UP).doubleValue()
//        //  成交量变化率
//        //val volChg = (curr.volume - prev.volume) / prev.volume
//        val volChg = new BigDecimal(curr.volume-prev.volume).divide(new BigDecimal(prev.volume), 4, RoundingMode.UP).doubleValue()
//        
//        val fp = FeaturePoint(ret, volChg, curr.amplitude, curr.bodyRatio)
//        fp
//      }
//    }
//  }
//
//  private def pointDist(a: FeaturePoint, b: FeaturePoint): Double = {
//    val dr = a.returnRate - b.returnRate
//    val dv = a.volumeChange - b.volumeChange
//    val da = a.amplitude - b.amplitude
//    val db = a.bodyRatio - b.bodyRatio
//    math.sqrt(dr * dr + dv * dv + da * da + db * db)
//  }
//
//  /** DTW 距离（四维特征，带 Sakoe-Chiba 窗口约束） */
//  def dtwDistance(s1: Seq[FeaturePoint], s2: Seq[FeaturePoint]): Double = {
//    val n = s1.size
//    val m = s2.size
//    val dp = Array.fill(n, m)(Double.MaxValue)
//    dp(0)(0) = pointDist(s1.head, s2.head)
//
//    for (i <- 1 until n; j <- math.max(1, i - 2) until math.min(m, i + 3)) {
//      val cost = pointDist(s1(i), s2(j))
//      val minPrev = math.min(
//        math.min(dp(i - 1)(j), dp(i)(j - 1)),
//        dp(i - 1)(j - 1)
//      )
//      dp(i)(j) = cost + minPrev
//    }
//    dp(n - 1)(m - 1)
//  }
// 
//
//  /** 写法 B: 命令式 for 循环 */
//  def findSimilarImperative(
//                             target: Seq[FeaturePoint],
//                             allStocks: Map[String, Seq[Bar]],
//                             windowSize: Int,
//                             topK: Int
//                           ): Seq[SimilarResult] = {
//
//    val results = ArrayBuffer[SimilarResult]()
//
//    for ((code, bars) <- allStocks) {
//      for (start <- 0 until (bars.size - windowSize)) {
//        val window = bars.slice(start, start + windowSize + 1)
//        val features = extractFeatures(window, windowSize)
//        if (features.size == windowSize) {
//          results += SimilarResult(code, bars(start + windowSize).date, dtwDistance(target, features))
//        }
//      }
//    }
//
//    results.sortBy(_.distance).take(topK).toSeq
//  }
//
//  /** 生成模拟K线：带趋势、波动、随机影线 */
//  def genBars(code: String, days: Int, seed: Long): Seq[Bar] = {
//    val rnd = new Random(seed)
//    var prevClose = 10.0 + rnd.nextDouble() * 20
//    var volume = 1_000_000.0
//    val bars = ArrayBuffer[Bar]()
//
//    for (d <- 1 to days) {
//      // 当日开盘 = 昨收 × (1 + 跳空)
//      val gap = (rnd.nextDouble() - 0.5) * 0.02
//      val open = prevClose * (1 + gap)
//
//      // 当日收盘 = 开盘 × (1 + 日内涨跌)
//      val intraday = (rnd.nextDouble() - 0.5) * 0.06
//      val close = open * (1 + intraday)
//
//      // 最高/最低：在 open/close 之上/之下再加随机影线
//      val high = math.max(open, close) * (1 + rnd.nextDouble() * 0.02)
//      val low  = math.min(open, close) * (1 - rnd.nextDouble() * 0.02)
//
//      volume = volume * (1 + (rnd.nextDouble() - 0.5) * 0.5)
//
//      bars += Bar(f"2024-01-${d}%02d", open, high, low, close, volume)
//      prevClose = close
//    }
//    bars.toSeq
//  }
//
//  def getTargetBars(stockCode: String): Seq[Bar] = {
//    val ls = DataFrame.loadModelAnalysisDataSet.get(stockCode).get.map(e=>{
//      val bar = Bar(
//        e.trade_date,
//        e.open.toDouble,
//        e.high.toDouble,
//        e.low.toDouble,
//        e.close.toDouble,
//        e.vol.toDouble
//      )
//      bar
//    })
//
//    if(ls.size>6){
//      ls.take(6)
//    }
//    else {
//      List.empty
//    }
//  }
//
//  def getAllBars(stockCode: String): Seq[Bar] = {
//    if(DataFrame.getDataForSelect(stockCode)==null || DataFrame.getDataForSelect(stockCode).size > 0){
//      return List.empty
//    }
//    DataFrame.getDataForSelect(stockCode).map(e=>{
//      val bar = Bar(
//        e.trade_date,
//        e.open.toDouble,
//        e.high.toDouble,
//        e.low.toDouble,
//        e.close.toDouble,
//        e.vol.toDouble
//      )
//      bar
//    })
//  }
//
//  def main(args: Array[String]): Unit = {
//    val windowSize = 5 //滑动的窗口
//    val topK = 5 //返回前5个相似的
//
//    // 1. 目标股票
//    val targetBars = genBars("TARGET", 10, seed = 42L)
//    val tsCode = "000001.SZ"
////    val targetBars = getTargetBars(tsCode)
//    val targetWindow = targetBars.slice(targetBars.size - (windowSize + 1), targetBars.size)
//    val targetFeatures = extractFeatures(targetWindow, windowSize)
//
//    println("=== 目标形态（近5日特征）===")
//    println("日期         涨跌幅    量变     振幅     实体")
//    targetFeatures.zipWithIndex.foreach { case (f, i) =>
//      val bar = targetWindow(i + 1)
//      println(f"${bar.date}  ${f.returnRate * 100}%6.2f%%  ${f.volumeChange * 100}%6.2f%%  ${f.amplitude * 100}%6.2f%%  ${f.bodyRatio * 100}%6.2f%%")
//    }
//    println()
//
////    DataFrame.STOCKS_MAP.values().asScala.filter(!_.ts_code.equals(tsCode)).foreach(e=>{
////      val allStocks= getAllBars(e.ts_code)
////
////      val t2 = System.nanoTime()
////      val resB = findSimilarImperative(targetFeatures, allStocks, windowSize, topK)
////      val t3 = System.nanoTime()
////
////      println("=== 写法 B: 命令式 for 循环 ===")
////      resB.foreach(r => println(f"  ${r.stockCode}  截至 ${r.endDate}  距离=${r.distance}%.6f"))
////      println(f"  耗时: ${(t3 - t2) / 1e6}%.2f ms\n")
////
////    })
//
//
//
//    // 2. 模拟全市场
//    val allStocks: Map[String, Seq[Bar]] = (1 to 20).map { i =>
//      f"STOCK$i%02d" -> genBars(s"STOCK$i", 200, seed = i.toLong * 1000)
//    }.toMap
//
////    val allStocks: Map[String, Seq[Bar]] = DataFrame.STOCKS_MAP.values().asScala
////      .map(e=>{
////        e.ts_code -> getAllBars(e.ts_code)
////      }).toMap.filter(_._2.size>10)
//
//    // 3. 两种写法 
//    val t2 = System.nanoTime()
//    val resB = findSimilarImperative(targetFeatures, allStocks, windowSize, topK)
//    val t3 = System.nanoTime()
// 
//
//    println("=== 写法 B: 命令式 for 循环 ===")
//    resB.foreach(r => println(f"  ${r.stockCode}  截至 ${r.endDate}  距离=${r.distance}%.6f"))
//    println(f"  耗时: ${(t3 - t2) / 1e6}%.2f ms\n")
// 
//
//    // 4. 额外：打印目标窗口的完整K线，方便肉眼核对
//    println("\n=== 目标窗口原始K线 ===")
//    println("日期        开盘     最高     最低     收盘     成交量")
//    targetWindow.foreach { b =>
//      println(f"${b.date}  ${b.open}%6.2f  ${b.high}%6.2f  ${b.low}%6.2f  ${b.close}%6.2f  ${b.volume}%12.0f  ${if (b.isBullish) "阳" else "阴"}")
//    }
//  }
//
//}