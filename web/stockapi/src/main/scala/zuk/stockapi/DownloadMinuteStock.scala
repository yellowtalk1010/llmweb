package zuk.stockapi

import com.alibaba.fastjson2.{JSONArray, JSONObject, JSONWriter}
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

import java.math.BigDecimal
/***
 * 获取交易日分钟信息，在2点30分时执行
 */
object DownloadMinuteStock extends Download {


  def run(stockList: List[StockApiVo]) = {
    stockList.foreach(stockApiVO=>{
      try {
        val stockMinuteVoList = ListBuffer[StockMinuteVo]()
        val path = createMinutePath(stockApiVO.getApi_code)
        if(!new File(path).exists()){
          //如果未下载过
          val url = s"https://www.stockapi.com.cn/v1/base/min?token=${LoaderLocalStockData.TOKEN}&code=${stockApiVO.getApi_code}&all=0"
          val response = super.download(url, 10) //下载分时成交量数据
          if (StringUtils.isNotEmpty(response)) {
            val jsonArray = JSONObject.parseObject(response).get("data").asInstanceOf[JSONArray]

            val lines = jsonArray.asScala.map(e => {
              val line = JSONObject.toJSONString(e, JSONWriter.Feature.LargeObject)
              val stockMinuteVo = JSONObject.parseObject(line, classOf[StockMinuteVo])
              val newLine = if (StringUtils.isNotEmpty(stockMinuteVo.getTime) //交易时间
                && StringUtils.isNotEmpty(stockMinuteVo.getPrice) //价格
                && StringUtils.isNotEmpty(stockMinuteVo.getShoushu) //售出
                && StringUtils.isNotEmpty(stockMinuteVo.getDanShu) //金额
                && StringUtils.isNotEmpty(stockMinuteVo.getBsbz)
              ) {
                stockMinuteVoList += stockMinuteVo
                JSONObject.toJSONString(stockMinuteVo, JSONWriter.Feature.LargeObject)
              }
              else {
                //数据为空，理论上不存在
                ""
              }
              newLine
            }).filter(l => StringUtils.isNotEmpty(l)).toList

            FileUtils.writeLines(new File(path), lines.asJava)
          }
//          else {
//            //写入空文件
//            FileUtils.writeLines(new File(path), List().asJava)
//          }
        }

      }
      catch
        case exception: Exception =>
      finally {
        //
      }
    })
  }


  def getStockMinuteVos(stockCode: String): List[StockMinuteVo] = {
    val path = createMinutePath(stockCode)
    val file = new File(path)
    val stockMinuteVoList = ListBuffer[StockMinuteVo]()
    if(file.exists()){
      FileUtils.readLines(file, "UTF-8").asScala.foreach(line => {
        val stockMinuteVo = JSONObject.parseObject(line, classOf[StockMinuteVo])
        stockMinuteVoList += stockMinuteVo
      })
    }
    stockMinuteVoList.toList
  }


  private def createMinutePath(code: String): String = {
    val simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
    var date = simpleDateFormat.format(new Date())
    date = "zuk"
    val path = LoaderLocalStockData.STOCK_MINUTE + File.separator + date + File.separator + code + ".jsonl"
    path
  }

}
