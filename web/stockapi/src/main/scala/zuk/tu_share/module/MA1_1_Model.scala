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

  var stockDto: StockDto = null

  override def run(days: List[ModuleDay]): Unit = {
    if(days.size > 10){
      val head = days.head
      val list = days.take(10)
      val zList = list.filter(_.limit.equals(ModuleDay.Z)) //炸板次数
      if(zList.size>=2 //多个次炸板
        && (ListOrderCheck.isIncreasing(zList.map(_.close.toFloat)) || ListOrderCheck.isIncreasing(zList.map(_.high.toFloat))) //炸板收盘价递增
        && list(1).limit.equals(ModuleDay.Z) //上一个交易日炸板
        && list.head.close.toFloat > list.head.pre_close.toFloat //收盘价大于昨收价格
      ){
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

  override def getStockDto(): StockDto = this.stockDto

  override def desc(): String = {
    "合力炸板，主力强势介入"
  }

  override def winRate: Float = 0.0

  override def reference: Float = 0.0
}
