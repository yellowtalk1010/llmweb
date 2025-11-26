package zuk.stockapi


import com.alibaba.fastjson2.{JSONArray, JSONObject, JSONWriter}
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*

/***
 * 获取当日个股收盘后的信息
 */
object DownloadDayStock extends Download {

  def run(stockList: List[StockApiVo]): Unit = {
    val num = new AtomicInteger(0)
    while (stockList.size != num.get()) {
      val sdf = new SimpleDateFormat("yyyyMM")
      val ym = sdf.format(new Date)
//      val ym = "202509"
      val sdf1 = new SimpleDateFormat("yyyy-MM")
      val startTime = sdf1.format(new Date) + "-01"
//      val startTime = "2025-09-01"
      val sdf2 = new SimpleDateFormat("yyyy-MM-dd")
      val endTime = sdf2.format(new Date)
//      val endTime = "2025-09-30"
      System.out.println(ym + "\t" + startTime + "\t" + endTime)
      stockList
//        .filter(stockApiVO=>{
//          val path = LoaderLocalStockData.STOCK_DAY + File.separator + stockApiVO.getApi_code + File.separator + ym + ".jsonl"
//          println(s"${stockApiVO.getApi_code}，已下载完成")
//          val file  = new File(path)
//          if(file.exists()){
//            val st = FileUtils.readLines(new File(path), "UTF-8").size() < 8
//            if(!st){
//              num.incrementAndGet
//            }
//            st
//          }
//          else {
//            true
//          }
//
//        })
        .foreach(stockApiVO => {
          try {
            val url = "https://www.stockapi.com.cn/v1/base/day?token=" + LoaderLocalStockData.TOKEN + "&code=" + stockApiVO.getApi_code + "&startDate=" + startTime + "&endDate=" + endTime + "&calculationCycle=100"
            val response = super.download(url)
            if(StringUtils.isNotEmpty(response)){
              val jsonArray = JSONObject.parseObject(response).get("data").asInstanceOf[JSONArray]
              val lines = jsonArray.asScala.map(e => {
                val line = JSONObject.toJSONString(e, JSONWriter.Feature.LargeObject)
                val stockDayVo = JSONObject.parseObject(line, classOf[StockDayVo])
                val newLine = if (StringUtils.isNotEmpty(stockDayVo.getOpen) //开盘价
                  && StringUtils.isNotEmpty(stockDayVo.getTime) //交易时间
                  && StringUtils.isNotEmpty(stockDayVo.getCode) //代码
                  && StringUtils.isNotEmpty(stockDayVo.getAmount) //交易总金额
                  && StringUtils.isNotEmpty(stockDayVo.getChangeRatio) //相比上次收盘价的涨跌比率
                  && StringUtils.isNotEmpty(stockDayVo.getHigh) //最高价
                  && StringUtils.isNotEmpty(stockDayVo.getLow) //最低价
                  && StringUtils.isNotEmpty(stockDayVo.getTurnoverRatio) //换手率
                  && StringUtils.isNotEmpty(stockDayVo.getVolume) //交易量
                  && StringUtils.isNotEmpty(stockDayVo.getClose) //收盘
                ) {
                  JSONObject.toJSONString(stockDayVo, JSONWriter.Feature.LargeObject)
                }
                else {
                  println(s"${stockApiVO.getApi_code}，${stockApiVO.getName}，数据为空，可能停牌，${line}")
                  ""
                }
                newLine
              }).filter(l => StringUtils.isNotEmpty(l)).toList

              val path = LoaderLocalStockData.STOCK_DAY + File.separator + stockApiVO.getApi_code + File.separator + ym + ".jsonl"
              val file = new File(path)
              if(!file.exists()){
                file.getParentFile.mkdirs()
                file.createNewFile()
              }
              FileUtils.writeLines(file, lines.asJava)
              println(stockApiVO.getApi_code + "，行数：" + lines.size + "，" + "成功，" + num.get + "/" + LoaderLocalStockData.STOCKS.size + "， " + startTime + "至" + endTime)
              if (lines.size == 0) {
                println(s"${stockApiVO.getApi_code}，${stockApiVO.getName}，下载数据为空。（可能停牌很久）")
              }
            }
          } catch {
            case e: Exception =>
              e.printStackTrace()
          } finally {
            num.incrementAndGet
          }
      })
    }
    println("完成全部个股的新数据更新，" + LoaderLocalStockData.STOCKS.size)
  }

}
