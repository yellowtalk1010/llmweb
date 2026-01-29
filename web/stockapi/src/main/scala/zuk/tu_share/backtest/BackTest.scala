package zuk.tu_share.backtest

import zuk.tu_share.DataFrame.config_properties
import zuk.tu_share.module.IModel
import zuk.utils.SendMail

import java.io.FileOutputStream
import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer

object BackTest {

  val backTestList = ListBuffer[IModel]()

  def analysis(): Unit = {

    val lines = new ListBuffer[String]()

    backTestList.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null)
      .groupBy(_.getClass.getSimpleName)
      .filter(_._2.size>0)
      .foreach(e=>{
        val clsName = e._1
        val ls = e._2
        val victoryList = ls.sortBy(e=>(e.buy.trade_date, e.getStockDto().preChangeRate)).reverse
          .filter(mod=>{
            if(mod.sells.size==0){
              val line = s"${clsName}, ${mod.buy.ts_code}, ${mod.buy.name},【${mod.getStockDto().totalMV}亿，${mod.getStockDto().limitUp}，${mod.getStockDto().limitDown}，${mod.getStockDto().turnoverRate}，涨跌${mod.getStockDto().preChangeRate}】, ${mod.buy.trade_date}【买入】, 【未交易】"
              lines += line
            }
            mod.sells.size>0
          })
          .filter(mod => {

            var st = false
            val preClose = mod.sells.head.pre_close
            val highStr = mod.sells.map(e => {
              val change = ((new BigDecimal(e.high).subtract(new BigDecimal(preClose))).multiply(new BigDecimal(100))).divide(new BigDecimal(preClose), 4, RoundingMode.UP)
              if(change.compareTo(new BigDecimal(0.45)) >=0){
                st = true //算入手续费
              }
              s"${e.trade_date}【${change}】【卖出】"
            }).mkString(", ")

            val ok = if (st) "" else "X"

            val line = s"${clsName}, ${mod.buy.ts_code}, ${mod.buy.name},【${mod.getStockDto().totalMV}亿，${mod.getStockDto().limitUp}，${mod.getStockDto().limitDown}，${mod.getStockDto().turnoverRate}，涨跌${mod.getStockDto().preChangeRate}】, ${mod.buy.trade_date}【买入】, ${highStr}, ${ok}"
            lines += line
            println(line)

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
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}【backtest】", htmlContent)
  }

}
