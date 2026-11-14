package zuk.tu_share.module

import zuk.similar.{SimilartyDto, SimilartyUtil}
import zuk.tu_share.dto.ModuleHistoryDailyData
import zuk.tu_share.utils.Dataset_moneyflow_dir

import java.math.{BigDecimal, RoundingMode}

/***
 * 1. 多日收盘价就是最高价（或相比最高价下跌1%个点）
 * 2. 多日主力净流入为正，但是股票确实震荡下跌
 *
 * 只能当潜力股，主力吸筹股来分析
 */
class MA10_Model extends IModel {

  var stockDto: StockDto = _
  var reason = ""

  override def buyReason(): String = this.reason

  override def getStockDto(): StockDto = {
    stockDto
  }

  override def backTestStep: Int = 15

  private def moneyflow_amounNet(day: ModuleHistoryDailyData): Boolean = {
    val moneyflowList = Dataset_moneyflow_dir.getTsCode(day.ts_code)
    if(moneyflowList!=null && moneyflowList.size > 0){
      val list = moneyflowList.filter(_.trade_date.equals(day.trade_date))
      if(list.size > 0 && list.head.net_amount.toFloat > 10 //主力资金净流入
//        && day.change.toFloat < 0   // 跌下降
      ){
        return true
      }
    }

    return false
  }

  /***
   * 收盘价就是最高价（或相比最高价下跌1%个点）
   */
  private def close_equal_high(day: ModuleHistoryDailyData): Boolean = {
    val rate = new BigDecimal(day.high.toFloat - day.close.toFloat)
      .divide(new BigDecimal(day.high.toFloat), 2, RoundingMode.UP).floatValue()
    rate <= 0.01  
      && day.change.toFloat < 7 
      && day.change.toFloat > 0
  }

  override def run(days: List[ModuleHistoryDailyData]): Unit = {
    val LEN = 15 //历史10个交易日
    if(days.size > LEN){
      val takeDays = days.take(LEN)
      val head = takeDays.head
      val moneyflowList = Dataset_moneyflow_dir.getTsCode(head.ts_code)
      val subDays = takeDays.filter(e=>{
        close_equal_high(e) || moneyflow_amounNet(e)
      })

      if(new BigDecimal(subDays.size).divide(new BigDecimal(takeDays.size), 2, RoundingMode.UP).floatValue() > 0.7){
        val tsStock = super.findTsStock(head.ts_code)
        this.stockDto = new StockDto(head.trade_date, tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
      }

    }
  }


  override def desc(): String = {
    "潜在的主力吸筹（15天回测）"
  }

  override def reference: Float = {
    0.0
  }

  override def warnUpperShadow: Boolean = {
    false
  }

}
