package zuk.tu_share.backtest

import zuk.tu_share.module.IModel

import java.math.{BigDecimal, RoundingMode}
import scala.collection.mutable.ListBuffer

object BackTest {

  val backTestList = ListBuffer[IModel]()

  def analysis(): Unit = {

    backTestList.filter(e=>e.getTsStocks()!=null && e.getTsStocks().size>0)
      .groupBy(_.getClass.getSimpleName)
      .filter(_._2.size>0)
      .foreach(e=>{
        val clsName = e._1
        val ls = e._2
        val victoryList = ls.filter(mod => {

          val preClose = mod.sells.head.pre_close
          val highStr = mod.sells.map(e => {
            val change = ((new BigDecimal(e.high).subtract(new BigDecimal(preClose))).multiply(new BigDecimal(100))).divide(new BigDecimal(preClose), 4, RoundingMode.UP)
            s"${e.trade_date}【${change}】【卖出】"
          }).mkString(", ")

          val st = mod.sells.filter(e => {
            e.high.toFloat > mod.buy.close.toFloat
          }).size > 0

          val ok = if (st) "" else "X"
          println(s"${mod.buy.ts_code}, ${mod.buy.name}, ${mod.buy.trade_date}【买入】, ${highStr}, ${ok}")

          st

        })

        println(s"${clsName}胜率：${new BigDecimal(victoryList.size).divide(new BigDecimal(ls.size), 4, RoundingMode.UP)}")
    })


  }

}
