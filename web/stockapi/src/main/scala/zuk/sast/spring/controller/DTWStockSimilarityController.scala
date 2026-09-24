package zuk.sast.spring.controller

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

@RestController
@RequestMapping(value = Array("stock_similar"))
@Component
class DTWStockSimilarityController {

  @GetMapping(value = Array("getTsCode"))
  def getTsCode(tsCode: String): java.util.Map[String, Object] = {

    val windowSize = 5 //滑动的窗口

    // 1. 目标股票
//    val tsCode = "000001.SZ"
    val targetBars = DTWStockSimilarity_B.getTargetBars(tsCode).take(windowSize)
    val targetFeatures = targetBars.toList.map(_.feature)

    println("=== 目标形态（近5日特征）===")
    println("日期         涨跌幅    量变     振幅     实体")
    targetBars.toList.zipWithIndex.foreach { tp2 =>
      val bar = tp2._1
      val f = bar.feature
      val i = tp2._2
      println(f"${bar.date}  ${f.returnRate * 100}%6.2f%%  ${f.volumeChange * 100}%6.2f%%  ${f.amplitude * 100}%6.2f%%  ${f.bodyRatio * 100}%6.2f%%")
    }

    // 2. 模拟全市场

    val allStocks: Map[String, Seq[Bar]] = DataFrame.STOCKS_MAP.values().asScala
      .map(e => {
        e.ts_code -> DTWStockSimilarity_B.getAllBars(e.ts_code)
      }).toMap.filter(_._2.size > 10)

    // 3. 两种写法
    val t2 = System.nanoTime()
    val resB = DTWStockSimilarity_B.findSimilarImperative(targetBars, allStocks, windowSize)
    val filterResB = resB.sortBy(_.distance).filter(e=>e.distance < 1).take(100)
    val t3 = System.nanoTime()


    println("=== 写法 B: 命令式 for 循环 ===")
    var hitsTotal = 0
    var stTotal = 0
    filterResB.foreach(res => {
      val hits = ListBuffer[ModuleDay]()
      val ls = DataFrame.getDataForSelect(res.stockCode)
      for (i <- 0 until ls.size) {
        if (ls(i).trade_date.equals(res.endDate)) {
          var count = 0
          for (ii <- i to 0 by -1 if count < 3) {
            count = count + 1
            hits += ls(ii)
          }
        }
      }

      hitsTotal = hitsTotal + hits.size

      val st = hits.filter(e => e.high.toDouble > e.pre_close.toDouble
        && e.change.toDouble > 1
      ).size

      stTotal = stTotal + st

      println(f"  ${res.stockCode} ${res.stockName} ${res.startDate}至 ${res.endDate}  距离=${res.distance}%.6f  成功=${st > 0}") //相似度越小越相似

    })
    println(f"  耗时: ${(t3 - t2) / 1e6}%.2f ms\n")
  
    val rate = new BigDecimal(stTotal).divide(new BigDecimal(hitsTotal), 2, RoundingMode.UP)
    println(rate)


    val list = new ListBuffer[String]
    val result = new java.util.HashMap[String, Object]()
    result.put("data", list.asJava)
    result.put("code", "success")
    result

  }

}
