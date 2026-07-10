package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.ListOrderCheck

import java.math.{BigDecimal, RoundingMode}

/***
 * 底部放巨量
 */
class MA7_Model extends IModel {

  val num = 60
  var stockDto: StockDto = _

  override def getStockDto(): StockDto = stockDto

  override def run(days: List[ModuleDay]): Unit = {
    if (days.size < num){
      return
    }
    val head = days.head
    val size = 1
    val historyDays = days.slice(size, days.length)
    val historyHigh = historyDays.map(_.high.toFloat).max //过去历史最高价
    val avgVol = new BigDecimal(historyDays.map(_.vol.toFloat).sum).divide(new BigDecimal(historyDays.size), 2, RoundingMode.UP).floatValue() //平均交易量

    //最近20天
    val history_40 = days.slice(1, 20)
    val avgVol_40 = new BigDecimal(history_40.map(_.vol.toFloat).sum).divide(new BigDecimal(history_40.size), 2, RoundingMode.UP).floatValue()

//    val lastestDays = days.take(size)
//    val lastestLow = lastestDays.map(_.low.toFloat).min //最近最低价
//    val lastestMaxVol = lastestDays.map(_.vol.toFloat).max //最近最大的一次交易量


    val downRate = new BigDecimal(historyHigh - head.low.toFloat).divide(new BigDecimal(historyHigh),2,RoundingMode.UP) //相比最高价，跌去的比例
    val volRate = new BigDecimal(head.vol).divide(new BigDecimal(avgVol), 2, RoundingMode.UP).floatValue() //
    val volRate_40 = new BigDecimal(head.vol).divide(new BigDecimal(avgVol_40), 2, RoundingMode.UP).floatValue() //

    if(0.4 <= downRate.floatValue() //跌超4个点
      && volRate_40 > 2.0 //放量2倍
      && ListOrderCheck.isDecreasing(history_40.reverse.map(_.ma.ma30.floatValue()))
      && ListOrderCheck.isDecreasing(history_40.reverse.map(_.ma.ma20.floatValue()))
      && head.ma.ma30.floatValue() > head.ma.ma20.floatValue()
//      && head.ma.ma20.floatValue() > List(head.ma.ma10.floatValue(), head.ma.ma5.floatValue()).max
      && head.change.toFloat > 2.0
    ){

      val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
      val head = days.head
      stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
      stockDto.warningUpperShadow = super.upperShadow(days)


      if (StringUtils.isNotBlank(head.total_mv)) {
        stockDto.totalMV = new BigDecimal(head.total_mv).divide(new BigDecimal(10000), 2, RoundingMode.UP).floatValue()
        stockDto.preChangeRate = new BigDecimal(head.change).setScale(2, RoundingMode.HALF_UP).floatValue()
      }
      else {
        stockDto.totalMV = new BigDecimal(days(1).total_mv).divide(new BigDecimal(10000), 2, RoundingMode.UP).floatValue()
        stockDto.preChangeRate = new BigDecimal(days(1).change).setScale(2, RoundingMode.HALF_UP).floatValue()
      }

    }

  }



  override def desc(): String = "底部放巨量"

  override def winRate: Float = {
    val v = DataFrame.properties.get(classOf[MA5_Model].getSimpleName.toUpperCase)
    if (v != null) {
      v.toString.toFloat
    }
    else {
      0.8818
    }
  }

  override def reference: Float = 0.0

  override def warnUpperShadow: Boolean = false
}
