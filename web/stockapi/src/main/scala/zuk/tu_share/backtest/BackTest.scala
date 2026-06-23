package zuk.tu_share.backtest

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.{DataFrame, ParseCammandParam}
import zuk.tu_share.DataFrame.config_properties
import zuk.tu_share.module.IModel
import zuk.utils.SendMail

import java.io.{File, FileOutputStream}
import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

case class BackTestDto() {
  @BeanProperty var stockCode: String = null
  @BeanProperty var stockName: String = null
  @BeanProperty var stockType: String = null
  @BeanProperty var tradedate: String = null

}

object BackTest {

  /***
   * 保存回测的数据
   */
  val backTestList = ListBuffer[IModel]()

  def analysis(): Unit = {

    val lines = new ListBuffer[String]()
    val backTestDtoList = new ListBuffer[BackTestDto]


    backTestList.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null)
      .groupBy(_.getClass.getSimpleName)
      .filter(_._2.size>0)
      .foreach(e=>{
        val clsName = e._1
        val ls = e._2
        val victoryList = ls.sortBy(e=>(e.buy.trade_date, e.getStockDto().preChangeRate)).reverse.filter(mod=>{
            if(mod.sells.size==0){
              val tsStock = DataFrame.STOCKS_MAP.get(mod.buy.ts_code)
              val name = if(!tsStock.isEmpty){
                val url = tsStock.get.getEastmoneyURL()
                val href = s"<a href='${url}'>${mod.buy.name}</a>"
                href
              }
              else {
                mod.buy.name
              }

              val buyReason = s"<span title='${mod.buyReason()}'>买入</span>"

              val line = s"${clsName}, ${mod.buy.ts_code}, ${name},【${mod.getStockDto().totalMV}亿，${mod.getStockDto().limitUp}，${mod.getStockDto().limitDown}，${mod.getStockDto().turnoverRate}，涨跌${mod.getStockDto().preChangeRate}】, ${mod.buy.trade_date}【${buyReason}】, 【未交易】"
              lines += line

              val dto = new BackTestDto
              dto.stockType = clsName.toUpperCase.trim
              dto.stockCode = mod.buy.ts_code.trim
              dto.stockName = mod.buy.name.trim
              dto.tradedate = mod.buy.trade_date.trim
              backTestDtoList += dto


            }
            mod.sells.size>0
          }).filter(mod => {

            var st = false
            val preClose = mod.sells.head.pre_close
            val highStr = mod.sells.map(e => {
              val change = ((new BigDecimal(e.high).subtract(new BigDecimal(preClose))).multiply(new BigDecimal(100))).divide(new BigDecimal(preClose), 4, RoundingMode.UP)
              if(change.compareTo(new BigDecimal(ParseCammandParam.param.wrate)) >= 0){
                st = true //算入手续费
              }
              s"${e.trade_date}【${change}】【卖出】"
            }).mkString(", ")

            val ok = if (st) "" else "X"

            val tsStock = DataFrame.STOCKS_MAP.get(mod.buy.ts_code)
            val name = if (!tsStock.isEmpty) {
              val url = tsStock.get.getEastmoneyURL()
              val href = s"<a href='${url}'>${mod.buy.name}</a>"
              href
            }
            else {
              mod.buy.name
            }

            val buyReason = s"<span title='${mod.buyReason()}'>买入</span>"

            val line = s"${clsName}, ${mod.buy.ts_code}, ${name},【${mod.getStockDto().totalMV}亿，${mod.getStockDto().limitUp}，${mod.getStockDto().limitDown}，${mod.getStockDto().turnoverRate}，涨跌${mod.getStockDto().preChangeRate}】, ${mod.buy.trade_date}【${buyReason}】, ${highStr}, ${ok}"
            lines += line


            val dto = new BackTestDto
            dto.stockType = clsName.toUpperCase.trim
            dto.stockCode = mod.buy.ts_code.trim
            dto.stockName = mod.buy.name.trim
            dto.tradedate = mod.buy.trade_date.trim
            backTestDtoList += dto


          st
          })

        //计算胜率
        val victoryRate = new BigDecimal(victoryList.size).divide(new BigDecimal(ls.filter(_.sells.size>0).size), 4, RoundingMode.UP)
        //胜率保存到properties中
        zuk.tu_share.DataFrame.properties.put(clsName.toUpperCase, victoryRate.toString)
        val line = s"${clsName}胜率：${victoryRate}, ${ls.head.desc()}"
        lines += line
        println(line)
    })

    storeProperties()

//    FileUtils.writeLines(new File("MODEL_BACK_TEST_RESULT.txt"), backTestMapList.map(dto=>{JSONObject.toJSONString(dto, Feature.LargeObject)}).asJava)
    FileUtils.writeLines(new File("MODEL_BACK_TEST_RESULT.txt"), backTestDtoList.map(dto=>{JSONObject.toJSONString(dto, Feature.LargeObject)}).asJava)

    sendMail(lines.mkString("<br>\n"))

  }

  /***
   * 保存到文件中
   */
  private def storeProperties() = {
    var output: FileOutputStream = null
    try {
      import zuk.tu_share.DataFrame
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      val dateStr = sdf.format(new Date())
      output = new FileOutputStream(DataFrame.config_properties)
      DataFrame.properties.store(output, s"${dateStr} stock config")
    }
    catch
      case exception: Exception =>
    finally {
      if(output!=null){
        output.close()
      }
    }
  }

  private def sendMail(htmlContent: String) = {
    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}【backtest】【${ParseCammandParam.param.wrate} 】", htmlContent)
  }

}
