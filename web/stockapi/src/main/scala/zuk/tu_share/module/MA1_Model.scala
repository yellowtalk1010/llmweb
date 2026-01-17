package zuk.tu_share.module
import zuk.tu_share.dto.ModuleDay

/***
 * 从炸板（相比上一个交易日价涨倍量）角度分析选择股票的逻辑，因为炸板是主力行为
 * 1. 第一天，股票炸板，
 * 2. 第二天，缩量上涨，即价涨量缩；口诀：价涨量缩，一致看多，即主力、散户同时看多；（原因：是主力一次刻意制造的一个恐吓，把散户赶下车）
 * 3. 第三天，股票炸板
 * 4. 第四天，没有跌破第三天的最低价
 * 5. 第五天，创造了历史新高，量能比第四天还小（价涨量缩，一致看多）。买入点
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
