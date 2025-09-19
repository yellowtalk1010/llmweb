package zuk.stockapi


import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter

import org.apache.commons.io.FileUtils

import zuk.sast.rules.utils.HttpClientUtil

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*

import org.apache.commons.lang3.StringUtils

object DownloadDayStock {

  def run(): Unit = {
    val num = new AtomicInteger(0)
    //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
    //String ym = sdf.format(new Date());
    while (LoaderLocalStockData.STOCKS.size != num.get()) {
      val sdf = new SimpleDateFormat("yyyyMM")
      val ym = sdf.format(new Date)
      val sdf1 = new SimpleDateFormat("yyyy-MM")
      val startTime = sdf1.format(new Date) + "-01"
      val sdf2 = new SimpleDateFormat("yyyy-MM-dd")
      val endTime = sdf2.format(new Date)
      System.out.println(ym + "\t" + startTime + "\t" + endTime)
      LoaderLocalStockData.STOCKS.asScala.foreach((stockApiVO: StockApiVo) => {
        val path = LoaderLocalStockData.STOCK_DAY + File.separator + stockApiVO.getApi_code + File.separator + ym + ".jsonl"
        val url = "https://stockapi.com.cn/v1/base/day?token=" + LoaderLocalStockData.TOKEN + "&code=" + stockApiVO.getApi_code + "&startDate=" + startTime + "&endDate=" + endTime + "&calculationCycle=100"
        try {
          //不存在
          val response = HttpClientUtil.sendGetRequest(url)
          val jsonObject = JSONObject.parseObject(response)
          val resCode = jsonObject.get("code").asInstanceOf[Integer]
          val resMsg = jsonObject.get("msg").asInstanceOf[String]
          if ((resCode eq 20000) && resMsg == "success") {
            val jsonArray = JSONObject.parseObject(response).get("data").asInstanceOf[JSONArray]
            val lines = jsonArray.stream.map((e: AnyRef) => {
              val line = JSONObject.toJSONString(e, JSONWriter.Feature.LargeObject)
              val stockDayVo = JSONObject.parseObject(line, classOf[StockDayVo])
              if (StringUtils.isNotEmpty(stockDayVo.getOpen) //开盘价
                && StringUtils.isNotEmpty(stockDayVo.getTime)//交易时间
                && StringUtils.isNotEmpty(stockDayVo.getCode)//代码
                && StringUtils.isNotEmpty(stockDayVo.getAmount)//交易总金额
                && StringUtils.isNotEmpty(stockDayVo.getChangeRatio)//相比上次收盘价的涨跌比率
                && StringUtils.isNotEmpty(stockDayVo.getHigh)//最高价
                && StringUtils.isNotEmpty(stockDayVo.getLow)//最低价
                && StringUtils.isNotEmpty(stockDayVo.getTurnoverRatio)//换手率
                && StringUtils.isNotEmpty(stockDayVo.getVolume)//交易量
                && StringUtils.isNotEmpty(stockDayVo.getClose) //收盘
              ) {
                JSONObject.toJSONString(stockDayVo, JSONWriter.Feature.LargeObject)
              }
              else {
                println(line + "， 数据为空")
              }
            }).toList
            FileUtils.writeLines(new File(path), lines)
            println(stockApiVO.getApi_code + "，行数：" + lines.size + "，" + "成功，" + num.get + "/" + LoaderLocalStockData.STOCKS.size + "， " + startTime + "至" + endTime)
            if (lines.size == 0) System.out.println(stockApiVO.getApi_code + "， 下载数据为空。（可能停牌）")
        }
        else
        {
          //failStocks.add(stockApiVO.getApi_code)
          println(stockApiVO.getApi_code + "，返回数据异常" + response)
        }
      }
      catch {
        case e: Exception =>
          //failStocks.add(stockApiVO.getApi_code)
          e.printStackTrace()
          println(url + "\n" + path + "\n失败")
      }
        finally {
          num.incrementAndGet
        }
      })
    }
    println("完成全部个股的新数据更新，" + LoaderLocalStockData.STOCKS.size)
  }

}
