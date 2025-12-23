package zuk.tu_share.backtest

import zuk.tu_share.module.IModel
import zuk.utils.SendMail

import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer

object BackTest {

  val backTestList = ListBuffer[IModel]()

  def analysis(): Unit = {

    val lines = new ListBuffer[String]()

    backTestList.filter(e=>e.getTsStocks()!=null && e.getTsStocks().size>0)
      .groupBy(_.getClass.getSimpleName)
      .filter(_._2.size>0)
      .foreach(e=>{
        val clsName = e._1
        val ls = e._2
        val victoryList = ls.filter(mod => {

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

          val line = s"${clsName}, ${mod.buy.ts_code}, ${mod.buy.name}, ${mod.buy.trade_date}【买入】, ${highStr}, ${ok}"
          lines += line
          println(line)

          st

        })

        val line = s"${clsName}胜率：${new BigDecimal(victoryList.size).divide(new BigDecimal(ls.size), 4, RoundingMode.UP)}"
        lines += line
        println(line)
    })


    sendMail(lines.mkString("<br>/n"))


  }

  private def sendMail(htmlContent: String) = {
    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}", htmlContent)
  }

}
