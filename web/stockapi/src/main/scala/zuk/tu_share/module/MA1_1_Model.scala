package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.ListOrderCheck

import java.math.{BigDecimal, RoundingMode}

/***
 * 合力炸板
 */
class MA1_1_Model extends IModel {

  var reason: String = ""
  var stockDto: StockDto = null
  var max = 6

  override def buyReason(): String = reason

  override def run(days: List[ModuleDay]): Unit = {
    if(days.size > max){
      val head = days.head
      val list = days.take(max)

      val zhaBanIndexList = list.zipWithIndex.filter(_._1.limit.equals("Z"))

      if(zhaBanIndexList.size>=2  //多个次炸板
        && Math.abs(zhaBanIndexList(0)._2 - zhaBanIndexList(1)._2) > 1 //但不能是连续炸板
      ){
        val middleList = list.slice(zhaBanIndexList(0)._2 + 1, zhaBanIndexList(1)._2) //最近两次炸板中间的数据


        val zList = zhaBanIndexList.map(_._1) //炸板列表

        if (middleList.filter(e=>List(ModuleDay.D, ModuleDay.U).contains(e.limit)).size==0 //炸板中间不能出现涨停、跌停
          && middleList.map(_.high.toFloat).max < zList.map(_.high.toFloat).max
          && middleList.map(_.low.toFloat).min > zList.map(_.low.toFloat).min
          && middleList.map(_.vol.toFloat).max < zList.map(_.vol.toFloat).min
          && list(1).limit.equals(ModuleDay.Z) //上一个交易日炸板
          && list(0).vol.toFloat < zList.map(_.vol.toFloat).min //缩量
        ) {
            reason = zList.reverse.map(_.trade_date).mkString("至")
            val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
            stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
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


    }
  }

  override def getStockDto(): StockDto = this.stockDto

  override def desc(): String = {
    "合力炸板，主力强势介入"
  }

  override def winRate: Float = 0.0

  override def reference: Float = 0.0
}

