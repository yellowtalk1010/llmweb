package zuk.tu_share.module

import zuk.similar.{SimilartyDto, SimilartyUtil}
import zuk.tu_share.dto.ModuleHistoryDailyData

/***
 * 1. 多日收盘价就是最高价（或相比最高价下跌1%个点）
 * 2. 多日主力净流入为正，但是股票确实震荡下跌
 * 
 * 只能当潜力股，主力吸筹股来分析
 */
class MA10_Model extends IModel {

  var stockDto: StockDto = _
  var reason = ""
  var similartyDto: SimilartyDto = null

  override def buyReason(): String = this.reason

  override def getStockDto(): StockDto = {
    stockDto
  }

  override def backTestStep: Int = 10

  override def run(days: List[ModuleHistoryDailyData]): Unit = {
    val LEN = 10 //历史10个交易日
    if(days.size > LEN){
      
    }
  }


  override def desc(): String = {
    "潜在的主力吸筹"
  }

  override def reference: Float = {
    0.0
  }

  override def warnUpperShadow: Boolean = {
    false
  }

  override def getSimilartyDto(): SimilartyDto = this.similartyDto
}
