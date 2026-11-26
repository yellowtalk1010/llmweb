package zuk.tu_share.module

import zuk.similar.{SimilartyDto, SimilartyUtil}
import zuk.tu_share.dto.ModuleHistoryDailyData
import zuk.tu_share.utils.Dataset_moneyflow_dir

import java.math.{BigDecimal, RoundingMode}

/***
 * 7个交易日涨幅50%以上
 */
class MA11_Model extends IModel {

  var stockDto: StockDto = _
  var reason = ""

  override def buyReason(): String = this.reason

  override def getStockDto(): StockDto = {
    stockDto
  }

  override def backTestStep: Int = 10

  override def run(days: List[ModuleHistoryDailyData]): Unit = {
    val LEN = 10 //历史10个交易日
    if(days.size > LEN){
      val takeDays = days.take(LEN)
      val head = days.head
      val list = days.take(LEN).filter(e=>{
        val lowChange = new BigDecimal(head.high.toFloat - e.low.toFloat).divide(new BigDecimal(e.low.toFloat), 4, RoundingMode.UP).floatValue() * 100
        lowChange >= 50
      })
      if(list.size > 0){
        reason = s"相比${list.head.trade_date}最低价涨幅50%以上"
        val tsStock = super.findTsStock(head.ts_code)
        this.stockDto = new StockDto(head.trade_date, tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
      }
    }
  }


  override def desc(): String = {
    "7天涨幅50%以上"
  }

  override def reference: Float = {
    0.0
  }

  override def warnUpperShadow: Boolean = {
    false
  }

}
