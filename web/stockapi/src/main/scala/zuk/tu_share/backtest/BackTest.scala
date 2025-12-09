package zuk.tu_share.backtest

import zuk.tu_share.module.IModel

import java.math.{BigDecimal, RoundingMode}
import scala.collection.mutable.ListBuffer

object BackTest {

  val backTestList = ListBuffer[IModel]()

  def analysis(clsName: String): Unit = {

    val filterList = backTestList.filter(_.getClass.getSimpleName.equals(clsName)).filter(e=>e.getTsStocks()!=null && e.getTsStocks().size>0)
    if(filterList.size==0){
      println("无数据")
      return
    }

    val victoryList = filterList.filter(mod=>{

      val preClose = mod.sells.head.pre_close
      val highStr = mod.sells.map(e=>{
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

    println(s"${clsName}胜率：${new BigDecimal(victoryList.size).divide(new BigDecimal(filterList.size), 4, RoundingMode.UP)}")

  }

}
