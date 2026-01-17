package zuk.tu_share.module
import zuk.tu_share.dto.ModuleDay

/***
 * 从炸板角度分析选择股票的逻辑，因为炸板是主力行为
 * 1. 如果股票炸板后，还能缩量上涨；
 * 2.
 */
class MA1_Model extends IModel {

  var stockDto: StockDto = _

  override def run(days: List[ModuleDay]): Unit = {

  }

  override def getStockDto(): StockDto = this.stockDto

  override def desc(): String = {
    "炸板角度"
  }

  override def winRate: Float = {
    0.0
  }

  override def reference: Float = 0.0
}
