package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay

import java.math.{BigDecimal, RoundingMode}

class MA3_1_Model extends IModel {

  var stockDto: StockDto = _

  override def run(days: List[ModuleDay]): Unit = {
    val changeUpRate = super.changeUpRate(days) //过去30个交易日中换手率大于3.5的占比，必须大于3成
    if(days.size>=4
//      && changeUpRate >= 0.3
    ){
      val list = days.take(4)
      val head = list.head
      if (
        filterPriceLimitUp(list.head)
          && list(0).ma.ma5.compareTo(list(0).ma.ma10) >= 0
          && list(1).ma.ma5.compareTo(list(1).ma.ma10) <= 0
          && list(2).ma.ma5.compareTo(list(2).ma.ma10) <= 0
          && new BigDecimal(head.turnover_rate).compareTo(BigDecimal(0)) >= 0 //换手率
          && new BigDecimal(head.turnover_rate).compareTo(BigDecimal(5)) <= 0 //换手率
          && (new BigDecimal(list(1).turnover_rate).compareTo(BigDecimal(4)) > 0
            || new BigDecimal(list(2).turnover_rate).compareTo(BigDecimal(4)) > 0) //换手率
          && new BigDecimal(head.change).compareTo(BigDecimal(4)) >= 0 //涨幅度
          && new BigDecimal(head.change).compareTo(BigDecimal(9)) <= 0 //涨幅度
          && new BigDecimal(head.vol).compareTo(new BigDecimal(list(1).vol).add(new BigDecimal(list(2).vol))) < 0 //
          && new BigDecimal(head.high).compareTo(new BigDecimal(list(1).high)) > 0
      ) {
        //缩量上涨
        //ST不推荐
        val tsStock = DataFrame.STOCKS_MAP.get(head.ts_code).getOrElse(null)
        if (tsStock != null) {
          stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), changeUpRate)
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
  }

  override def getStockDto(): StockDto = stockDto

  override def desc(): String = "缩量上涨，建议下午买入"

  override def winRate: Float = {
    val v = DataFrame.properties.get(classOf[MA3_1_Model].getSimpleName.toUpperCase)
    if (v != null) {
      v.toString.toFloat
    }
    else {
      0.9118
    }
  }

  override def reference: Float = 0.00

}
