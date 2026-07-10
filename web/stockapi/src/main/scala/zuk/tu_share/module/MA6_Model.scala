package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.ListOrderCheck

import java.math.{BigDecimal, RoundingMode}

/***
 * 超越MA30
 */
class MA6_Model extends IModel {

  val num = 10
  var stockDto: StockDto = _

  override def getStockDto(): StockDto = stockDto

  override def run(days: List[ModuleDay]): Unit = {
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
//        && day.ma.ma10.compareTo(day.ma.ma30) > 0
//        && day.ma.ma30.compareTo(day.ma.ma20) > 0
    })

    val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
    if(ls.size == num
      && tsStock != null
      && ListOrderCheck.isDecreasing(list.map(_.ma.ma5.floatValue())) //ma5是递增的
      && ListOrderCheck.isDecreasing(list.map(_.ma.ma10.floatValue())) //ma10是递增的
//      && ListOrderCheck.isDecreasing(list.map(_.ma.ma20.floatValue())) //ma20是递增的
//      && ListOrderCheck.isDecreasing(list.map(_.ma.ma30.floatValue())) //ma30是递增的

      && calaChange(list)
//      && calaVol(list)
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

  private def calaVol(list: List[ModuleDay]): Boolean = {

    val preList = list.reverse.take(7)
    val sufList = list.reverse.slice(7, list.size)

    //平均交易量
    val avgVol = new BigDecimal(preList.map(_.vol.toFloat).sum).divide(new BigDecimal(preList.size), 2, RoundingMode.UP)
    //最大交易量
    val maxVol = new BigDecimal(preList.map(_.vol.toFloat).max)


    val moreList = sufList.map(_.vol.toFloat).filter(v=>{
      new BigDecimal(v).divide(avgVol, 2, RoundingMode.UP).compareTo(new BigDecimal(2)) >= 0 //连续多天放3倍量
    })

    moreList.size == sufList.size
  }

  /**
   * 连续上涨
   */
  private def calaChange(list: List[ModuleDay]): Boolean = {
//    val sufList = list.reverse.slice(5, list.size)
//    val st = sufList.filter(_.change.toFloat > 0.0).size == sufList.size

    val result = new BigDecimal(list.filter(_.change.toFloat > 0).size).divide(new BigDecimal(list.size), 2, RoundingMode.UP)
    val st1 = result.compareTo(new BigDecimal(0.7)) > 0

    val max = list.slice(1, list.size).map(_.high.toFloat).max
    val st2 = new BigDecimal(list.head.high).divide(new BigDecimal(max), 2, RoundingMode.UP).compareTo(new BigDecimal(0.9)) > 0

    st1 && st2

//    var n = 0
//    for (i <- 1 until sufList.size) {
//      val pre = sufList(i-1)
//      val preLowest = List(pre.open.toFloat, pre.low.toFloat, pre.high.toFloat, pre.close.toFloat).min
//      val cur = sufList(i)
//      val curMaxest = List(cur.open.toFloat, cur.low.toFloat, cur.high.toFloat, cur.close.toFloat).max
//      if(curMaxest > preLowest){
//        n = n + 1
//      }
//    }

//    st
  }

  override def desc(): String = "超越MA30"

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
