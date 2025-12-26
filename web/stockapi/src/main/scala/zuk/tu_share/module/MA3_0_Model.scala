package zuk.tu_share.module

import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay

import java.math.BigDecimal

class MA3_0_Model extends IModel {

  var stockDto: StockDto = _

  override def run(days: List[ModuleDay]): Unit = {
    if(days.size>=3){
      val list = days.take(3)
      val head = list.head
      if (
        list(0).ma.ma5.compareTo(list(0).ma.ma10) >= 0          // 当前交易日，穿过5日线
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
          stockDto = new StockDto(tsStock, super.limitUp(days), super.changeUpRate(days))
        }
      }
    }
  }

  override def getStockDto(): StockDto = stockDto

  override def desc(): String = "上穿MA5"

  override def winRate: Float = 0.8379

  override def reference: Float = 0.00
}
