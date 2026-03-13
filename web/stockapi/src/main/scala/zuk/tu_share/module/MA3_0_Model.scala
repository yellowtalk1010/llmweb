package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay

import java.math.{BigDecimal, RoundingMode}

class MA3_0_Model extends IModel {

  var stockDto: StockDto = _

  override def run(days: List[ModuleDay]): Unit = {
    if(days.size>30){
      val list = days.take(30)
      val head = list.head
      if (filterPriceLimitUp(list.head)
          && list.filter(e=>e.limit.equals("Z") || e.limit.equals("U")).size > 0
          && list(0).ma.ma5.compareTo(list(0).ma.ma10) >= 0          // 当前交易日，穿过5日线
          && list(1).ma.ma5.compareTo(list(1).ma.ma10) <= 0     //上一个交易日，还在5日线下
          && list(2).ma.ma5.compareTo(list(2).ma.ma10) <= 0     //前一个交易日，还在5日线下
          && new BigDecimal(head.turnover_rate).compareTo(BigDecimal(4)) >= 0   //换手率
          && new BigDecimal(head.turnover_rate).compareTo(BigDecimal(15)) <= 0  //换手率
          && new BigDecimal(head.change).compareTo(BigDecimal(4)) >= 0  //涨幅度
          && new BigDecimal(head.change).compareTo(BigDecimal(7)) <= 0  //涨幅度
          && List(list(1).change.toFloat, list(2).change.toFloat).min < 0
      ) {
        val tsStock = DataFrame.STOCKS_MAP.get(head.ts_code).getOrElse(null)
        if(tsStock!=null){
          stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
          stockDto.warningUpperShadow = super.upperShadow(days)
          if(StringUtils.isNotBlank(head.total_mv)){
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

  override def desc(): String = "上穿MA5"

  override def winRate: Float = {
    val v = DataFrame.properties.get(classOf[MA3_0_Model].getSimpleName.toUpperCase)
    if (v != null) {
      v.toString.toFloat
    }
    else {
      0.8469
    }
  }

  override def reference: Float = 0.00
}
