package zuk.similar

import zuk.tu_share.DataFrame
import zuk.tu_share.utils.IncreateDecreateRateDescUtil

/***
 * 核心思路
 * 既然你要找的是“近5日涨跌 + 交易量”的相似形态，特征向量的设计很关键。不能直接用原始价格和成交量，因为不同股票的绝对数值差异巨大。
 *
 * 建议构建一个二维特征序列：
 * 维度1：每日涨跌幅（(今收-昨收)/昨收），去除价格量纲。
 * 维度2：每日成交量变化率（(今量-昨量)/昨量），去除成交量量纲。
 * 这样得到的是一条包含价格波动节奏和量能变化节奏的“形状曲线”。然后对全市场股票的历史数据做滚动窗口（窗口大小=5），用 DTW 计算每个窗口与目标形态的距离，取距离最小的若干结果。
 *
 * @param date
 * @param close
 * @param volume
 */

case class Bar(date: String, close: Double, volume: Double)
case class FeaturePoint(returnRate: Double, volumeChange: Double)
case class SimilarResult(stockCode: String, endDate: String, distance: Double)

/***
 * 根据形态确定买、卖点
 */
object DTWStockSimilarity {

  /** 提取特征序列 */
  def extractFeatures(bars: Seq[Bar], windowSize: Int): Seq[FeaturePoint] = {
    if (bars.size < windowSize + 1) Seq.empty
    else (1 to windowSize).map { i =>
      val prev = bars(i - 1)
      val curr = bars(i)
      val ret = (curr.close - prev.close) / prev.close
      val volChg = (curr.volume - prev.volume) / prev.volume
      FeaturePoint(ret, volChg)
    }
  }

  private def pointDist(a: FeaturePoint, b: FeaturePoint): Double = {
    val dr = a.returnRate - b.returnRate
    val dv = a.volumeChange - b.volumeChange
    math.sqrt(dr * dr + dv * dv)
  }

  /** DTW 距离（二维，带窗口约束） */
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

  /** 全市场滑动窗口搜索 */
  def findSimilar(
                   target: Seq[FeaturePoint],
                   allStocks: Map[String, Seq[Bar]],
                   windowSize: Int,
                   topK: Int
                 ): Seq[SimilarResult] = {

    val results = for {
      (code, bars) <- allStocks.toSeq
      start <- 0 to (bars.size - windowSize - 1)
      window = bars.slice(start, start + windowSize + 1)
      features = extractFeatures(window, windowSize)
      if features.size == windowSize
    } yield SimilarResult(code, bars(start + windowSize).date, dtwDistance(target, features))

    results.sortBy(_.distance).take(topK)
  }

//  def run(): Unit = {
//    val stocks = TushareAllStocks.allStocks
//    stocks.foreach(stock=>{
//      val stockCode = stock.ts_code
//      val stockDailyDataList = TushareStockDailyDataComponent.getDailyDataList(stockCode)
//      println(stockDailyDataList.size)
//    })
//  }

}