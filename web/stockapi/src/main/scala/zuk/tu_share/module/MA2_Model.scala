package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.ListOrderCheck

import java.math.{BigDecimal, RoundingMode}

/***
 * 首板策略
 * 1. 出现首板；
 * 2. 在3-5天内缩量下跌；
 * 3. 出现地量且收下影线，就是买点；
 * 4. 后市缩量小阴小阳，安全持有；
 * 5. 超过8天不收付涨停价，马上出局。
 */
class MA2_Model extends IModel {

  val max = 10
  var stockDto: StockDto = _

  override def run(days: List[ModuleDay]): Unit = {
    try {
      if (days.size > max && days.take(max).filter(_.limit.equals(ModuleDay.U)).size == 1) {
        val index = days.take(max).indexWhere(e=>e.limit.equals(ModuleDay.U))
        if(index > 3){
          val list = days.slice(1, index)
          if(ListOrderCheck.isDecreasing(list.map(_.vol.toFloat))     //缩量
            && ListOrderCheck.isDecreasing(list.map(_.close.toFloat)) //下跌
            && (
            days.head.vol.toFloat > days(1).vol.toFloat
//             days.head.high.toFloat > days(1).high.toFloat
            &&
              days.head.change.toFloat > 0
            )
          ){
            val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
            stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
          }

        }

//        if(index == 3
//
//          && days(1).vol < days(2).vol
//          && days(2).vol < days(3).vol  //连续3天缩量
//
//
//          && days(1).close.toFloat < days(2).close.toFloat
//          && days(2).close.toFloat < days(3).close.toFloat //连续3天下跌
//
//        ){
//          val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
//          stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
//        }

//        val list = days.take(max)
//        val head = days.head
//        if (
//          filterPriceLimitUp(list.head)
//          && list(2).limit.equals("Z") //炸板
//          && list(2).high.equals(list(2).priceLimit.priceLimitUp.toString) //炸板后，尾盘修复
//          && list(1).high.toFloat > list(2).high.toFloat && list(1).vol.toFloat < list(2).vol.toFloat && list(1).change.toFloat > 0 //炸板次日价涨量缩
//          && list(0).high.toFloat > list(1).high.toFloat  //
//          && list(0).low.toFloat > list(1).low.toFloat    //
//          && list(0).vol.toFloat < list(1).vol.toFloat
//        ) {
//          val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
//          stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
//          if (StringUtils.isNotBlank(head.total_mv)) {
//            stockDto.totalMV = new BigDecimal(head.total_mv).divide(new BigDecimal(10000), 2, RoundingMode.UP).floatValue()
//            stockDto.preChangeRate = new BigDecimal(head.change).setScale(2, RoundingMode.HALF_UP).floatValue()
//          }
//          else {
//            stockDto.totalMV = new BigDecimal(days(1).total_mv).divide(new BigDecimal(10000), 2, RoundingMode.UP).floatValue()
//            stockDto.preChangeRate = new BigDecimal(days(1).change).setScale(2, RoundingMode.HALF_UP).floatValue()
//          }
//        }
      }
    }
    catch
      case exception: Exception => //exception.printStackTrace()

  }


  override def getStockDto(): StockDto = this.stockDto

  override def desc(): String = {
    "1.首板后；2.持续缩量下跌；3. 出现地量且收下引线（买点）；4. 后续小阴小阳，安全持有；5. 超过8天首付涨跌价，马上出局。"
  }

  override def winRate: Float = {
    val v = DataFrame.properties.get(classOf[MA2_Model].getSimpleName.toUpperCase)
    if(v!=null){
      v.toString.toFloat
    }
    else {
      0.9296
    }
  }

  override def reference: Float = 0.0
}
