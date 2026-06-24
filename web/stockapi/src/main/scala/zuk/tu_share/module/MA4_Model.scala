package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.ListOrderCheck

import java.math.{BigDecimal, RoundingMode}

class MA4_Model extends IModel {

  var stockDto: StockDto = _

  override def getStockDto(): StockDto = stockDto

  override def run(days: List[ModuleDay]): Unit = {
    val num = 5
    if (days.size < num){
      return
    }
    val head = days.head
    val list = days.take(num)
    if(list.filter(e=>e.ma==null
      || e.ma.ma5==null
      || e.ma.ma10==null
      || e.ma.ma20==null
      || e.ma.ma30==null).size>0){
      return
    }
    val ls = list.filter(day=>{
      day.ma.ma5.compareTo(day.ma.ma10) > 0
      && day.ma.ma10.compareTo(day.ma.ma20) > 0
      && day.ma.ma20.compareTo(day.ma.ma30) > 0
      && List(day.open, day.close, day.high, day.low).map(new BigDecimal(_)).filter(_.compareTo(day.ma.ma5) >= 0).size > 0 //上穿ma5
      && List(day.open, day.close, day.high, day.low).map(new BigDecimal(_)).filter(_.compareTo(day.ma.ma10) <= 0).size == 0 //不能下穿ma10
      && head.change.toFloat > 0
    })

    val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
    if(ls.size == num
      && tsStock != null
      && ListOrderCheck.isDecreasing(list.map(_.ma.ma5.floatValue())) //ma5是递增的
      && ListOrderCheck.isDecreasing(list.map(_.ma.ma10.floatValue())) //ma10是递增的
      && ListOrderCheck.isDecreasing(list.map(_.ma.ma20.floatValue())) //ma20是递增的
      && ListOrderCheck.isDecreasing(list.map(_.ma.ma30.floatValue())) //ma30是递增的
    ){
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

  override def desc(): String = "日线、周线开口上扬"

  override def winRate: Float = {
    val v = DataFrame.properties.get(classOf[MA4_Model].getSimpleName.toUpperCase)
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
