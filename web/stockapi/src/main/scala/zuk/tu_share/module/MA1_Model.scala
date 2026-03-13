package zuk.tu_share.module
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay

import java.math.{BigDecimal, RoundingMode}
/***
 * 中长持有
 *
 * 从炸板（相比上一个交易日价涨倍量）角度分析选择股票的逻辑，因为炸板是主力行为
 * 1. 第一天，股票炸板，
 * 2. 第二天，缩量上涨，即价涨量缩；口诀：价涨量缩，一致看多，即主力、散户同时看多；（原因：是主力一次刻意制造的一个恐吓，把散户赶下车）
 * 3. 第三天，股票炸板
 * 4. 第四天，没有跌破第三天的最低价
 * 5. 第五天，创造了历史新高，量能比第四天还小（价涨量缩，一致看多）。买入点
 */
class MA1_Model extends IModel {

  val max = 3
  var stockDto: StockDto = _

  override def run(days: List[ModuleDay]): Unit = {
    try {
      if (days.size > max && days.take(max).filter(_.limit.equals(ModuleDay.Z)).size > 0) {
        val list = days.take(max)
        val head = days.head
        if (
          filterPriceLimitUp(list.head)
          && list(2).limit.equals("Z") //炸板
          && list(2).high.equals(list(2).priceLimit.priceLimitUp.toString) //炸板后，尾盘修复
          && list(1).high.toFloat > list(2).high.toFloat && list(1).vol.toFloat < list(2).vol.toFloat && list(1).change.toFloat > 0 //炸板次日价涨量缩
          && list(0).high.toFloat > list(1).high.toFloat  //
          && list(0).low.toFloat > list(1).low.toFloat    //
          && list(0).vol.toFloat < list(1).vol.toFloat
        ) {
          val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
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
    }
    catch
      case exception: Exception => //exception.printStackTrace()

  }


  override def getStockDto(): StockDto = this.stockDto

  override def desc(): String = {
    "主力行为：1.炸板，2.次日缩量上涨；3.缩量上涨（尾盘买入）；4. 涨跌都必须卖出。因为如果下跌，会一直下跌。 "
  }

  override def winRate: Float = {
    val v = DataFrame.properties.get(classOf[MA1_Model].getSimpleName.toUpperCase)
    if(v!=null){
      v.toString.toFloat
    }
    else {
      0.9296
    }
  }

  override def reference: Float = 0.0
}
