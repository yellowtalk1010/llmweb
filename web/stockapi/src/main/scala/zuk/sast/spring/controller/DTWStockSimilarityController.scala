package zuk.sast.spring.controller

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

@RestController
@RequestMapping(value = Array("stock_similar"))
@Component
class DTWStockSimilarityController {

  private var trade_date = "999999999"

  //http://localhost:8080/stock_similar/getTsCode?tsCode=000001.SZ&tradeDate=20260924

  @GetMapping(value = Array("getTsCode"))
  def getTsCode(tsCode: String, tradeDate: String): java.util.Map[String, Object] = {

    val windowSize = 5 //滑动的窗口

    // 1. 目标股票
    if (StringUtils.isNotBlank(tradeDate)){
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
    val filterResB = resB.sortBy(_.distance).filter(e=> 0 < e.distance && e.distance < 0.5).take(100)
    val t3 = System.nanoTime()


    println("=== 写法 B: 命令式 for 循环 ===")
    var hitsTotal = 0
    var stTotal = 0
    filterResB.map(res => {
      val hits = ListBuffer[ModuleDay]()
      val ls = DataFrame.getDataForSelect(res.stockCode)
      for (i <- 0 until ls.size) {
        if (ls(i).trade_date.equals(res.endDate)) {
          var count = 0
          for (ii <- i to 0 by -1 if count < 3) { //预测未来3天的结果
            count = count + 1
            hits += ls(ii)
          }
        }
      }
      (res, hits)
    }).filter(tp2=>tp2._2.size>0).foreach(tp2=>{
      val res = tp2._1
      val hits = tp2._2
      
      val st = hits.filter(e => e.high.toDouble > e.pre_close.toDouble
        && e.change.toDouble > 1 //涨幅大于1个点
      ).size > 0

      hitsTotal = hitsTotal + 1
      if(st) {
        stTotal = stTotal + 1
      }

      println(f"  ${res.stockCode} ${res.stockName} ${res.startDate}至 ${res.endDate}  距离=${res.distance}%.6f  成功=${st}") //相似度越小越相似

    })
    println(f"  耗时: ${(t3 - t2) / 1e6}%.2f ms\n")

    if(hitsTotal == 0){
      println("无相似数据")
    }
    else {
      val rate = new BigDecimal(stTotal).divide(new BigDecimal(hitsTotal), 2, RoundingMode.UP)
      println(s"胜率 ${stTotal}/${hitsTotal}：" + rate)
      //样本要足够多，胜率足够大
    }

    val list = new ListBuffer[String]
    val result = new java.util.HashMap[String, Object]()
    result.put("data", list.asJava)
    result.put("code", "success")
    result

  }

}
