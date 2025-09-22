package zuk.stockapi


import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter

import org.apache.commons.io.FileUtils

import zuk.stockapi.utils.HttpClientUtil

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*

import org.apache.commons.lang3.StringUtils

object DownloadDayStock {


  def run(): Unit = {
    val num = new AtomicInteger(0)
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
          var response:String = null
          var isBreak = false
          val looptime = new AtomicInteger(0) //循环次数
          while (!isBreak){
            try {
              if(looptime.get()>0){
                println(s"第${looptime.get()}次重试请求:${url}")
              }
              response = HttpClientUtil.sendGetRequest(url)
              val jsonObject = JSONObject.parseObject(response)
              val resCode = jsonObject.get("code").asInstanceOf[Integer]
              val resMsg = jsonObject.get("msg").asInstanceOf[String]
              if (resCode.toString.equals("20000") && resMsg == "success") {
                isBreak = true
              }
              else {
                println(s"数据采集异常（${looptime.incrementAndGet()}次）：${url}")
                Thread.sleep(500) //等待500毫秒继续请求
              }
            }
            catch
              case exception: Exception =>
                exception.printStackTrace()
          }

          val jsonArray = JSONObject.parseObject(response).get("data").asInstanceOf[JSONArray]
          val lines = jsonArray.asScala.map(e => {
            val line = JSONObject.toJSONString(e, JSONWriter.Feature.LargeObject)
            val stockDayVo = JSONObject.parseObject(line, classOf[StockDayVo])
            val newLine = if (StringUtils.isNotEmpty(stockDayVo.getOpen) //开盘价
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
              println(s"${stockApiVO.getApi_code}，${stockApiVO.getName}，数据为空，可能停牌，${line}")
              ""
            }
            newLine
          }).filter(l=>StringUtils.isNotEmpty(l)).toList

          FileUtils.writeLines(new File(path), lines.asJava)

          println(stockApiVO.getApi_code + "，行数：" + lines.size + "，" + "成功，" + num.get + "/" + LoaderLocalStockData.STOCKS.size + "， " + startTime + "至" + endTime)
          if (lines.size == 0) {
            println(s"${stockApiVO.getApi_code}，${stockApiVO.getName}，下载数据为空。（可能停牌很久）")
          }

        } catch {
          case e: Exception =>
            //failStocks.add(stockApiVO.getApi_code)
            e.printStackTrace()
            println(url + "\n" + path + "\n失败")
        } finally {
          num.incrementAndGet
        }
      })
    }
    println("完成全部个股的新数据更新，" + LoaderLocalStockData.STOCKS.size)
  }

}
