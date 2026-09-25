package zuk.similar

import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

import scala.collection.mutable.ListBuffer
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, IncreateDecreateRateDescUtil}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Random
import scala.jdk.CollectionConverters.*
import java.math.{BigDecimal, RoundingMode}
import zuk.similar.*

import scala.beans.BeanProperty

class SimilartyDto(@BeanProperty stockCode: String){
  @BeanProperty var stockName: String = ""
  @BeanProperty var tradeDate: String = ""
  @BeanProperty var sampleNumber: Int = 0                       //样本数量
  @BeanProperty var winate: Float = 0.0                   //胜率
  @BeanProperty var desc: String = ""                     //描述
  @BeanProperty val sampleList = new java.util.ArrayList[String]   //样本详情
}


/***
 * 图形相似度
 */
object SimilartyUtil {

  def getTsCode(tsCode: String, tradeDate: String): SimilartyDto = {
    
    val similartyDto = new SimilartyDto(tsCode)
    similartyDto.stockName = DataFrame.STOCKS_MAP.get(tsCode).name
    similartyDto.tradeDate = tradeDate

    var trade_date = "999999999"
    
    val windowSize = 5 //滑动的窗口

    // 1. 目标股票
    if (StringUtils.isNotBlank(tradeDate)) {
      trade_date = tradeDate
    }
    println(s"tsCode: ${tsCode}, tradeDate:${tradeDate}")
    val targetBars = DTWStockSimilarity_B.getTargetBars(tsCode)
      .filter(_.date.toLong <= trade_date.toLong)
      .take(windowSize)
    val targetFeatures = targetBars.toList.map(_.feature)

    println("=== 目标形态（近5日特征）===")
    println("股票代码    股票名称    日期         涨跌幅    量变     振幅     实体")
    targetBars.toList.zipWithIndex.foreach { tp2 =>
      val bar = tp2._1
      val f = bar.feature
      val i = tp2._2
      println(f"${bar.stockCode}  ${bar.stockName}  ${bar.date}  ${f.returnRate * 100}%6.2f%%  ${f.volumeChange * 100}%6.2f%%  ${f.amplitude * 100}%6.2f%%  ${f.bodyRatio * 100}%6.2f%%")
    }

    // 2. 模拟全市场

    val allStocks: Map[String, Seq[Bar]] = DataFrame.STOCKS_MAP.values().asScala
      .map(e => {
        e.ts_code -> DTWStockSimilarity_B.getAllBars(e.ts_code)
      }).toMap.filter(_._2.size > 10)

    // 3. 两种写法
    val t2 = System.nanoTime()
    val resB = DTWStockSimilarity_B.findSimilarImperative(targetBars, allStocks, windowSize)
    val filterResB = resB.sortBy(_.distance).filter(e => 0 < e.distance && e.distance < 0.5).take(100)
    val t3 = System.nanoTime()


    println("=== 写法 B: 命令式 for 循环 ===")
    var hitsTotal = 0
    var stTotal = 0
    filterResB.map(res => {
      //验证相似的图形的胜率
      val hits = ListBuffer[ModuleDay]()
      val ls = DataFrame.getDataForSelect(res.stockCode)
      for (i <- 0 until ls.size) {
        if (ls(i).trade_date.equals(res.endDate)) {
          var count = 0
          for (ii <- i to 0 by -1 if count < 4) { //预测未来3天的结果
            count = count + 1
            hits += ls(ii)
          }
        }
      }
      (res, hits) //hits 中包含了需要比较的自己，在head中
    }).filter(tp2 => tp2._2.size >= 2).foreach(tp2 => {
      val res = tp2._1
      val hits = tp2._2
      
      val self = hits.head
      val compareList = hits.slice(1, hits.size)
      var changeList = ListBuffer[Double]()
      val st = compareList.filter(e=>{
        //涨跌幅
        val change = new BigDecimal((e.high.toDouble - self.close.toDouble) * 100).divide(new BigDecimal(self.close.toDouble), 4, RoundingMode.UP).doubleValue()
        changeList += change
        val st = e.high.toDouble > self.close.toDouble && change > 1
        st
      }).size > 0
      
//      val st = hits.filter(e => e.high.toDouble > e.pre_close.toDouble
//        && e.change.toDouble > 1 //涨幅大于1个点
//      ).size > 0

      hitsTotal = hitsTotal + 1
      if (st) {
        stTotal = stTotal + 1
      }

      val str = f"  ${res.stockCode} ${res.stockName} ${res.startDate}至 ${res.endDate}  距离=${res.distance}%.6f  成功=${st},  涨幅=${changeList.map(_.toString).mkString(", ")}"
      similartyDto.sampleList.add(str)
      println(str) //相似度越小越相似

    })


    similartyDto.desc = if (hitsTotal == 0) {
      s"无相似数据"
    }
    else {
      val rate = new BigDecimal(stTotal).divide(new BigDecimal(hitsTotal), 2, RoundingMode.UP)
      similartyDto.winate = rate.floatValue() //样本胜率
      similartyDto.sampleNumber = hitsTotal //样本数量
      s"${stTotal}/${hitsTotal}=" + rate
      //样本要足够多，胜率足够大
    }

    
    println(f"\n=====================================【${tsCode} ${DataFrame.STOCKS_MAP.get(tsCode).name}  胜率：${similartyDto.desc}】 耗时: ${(t3 - t2) / 1e6}%.2f ms\n")

    similartyDto
  }
  
}
