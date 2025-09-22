package zuk.stockapi.model

import zuk.stockapi.utils.CloseIncreaseUtil
import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math.BigDecimal

/***
 * 刚刚M5>M20>M30 金叉
 */
class MA2_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  var isOK: Boolean = false

  override def isHit(): Boolean = this.isOK

  override def run(): Unit = {
    val len = 3
    if(maList.size > len){
      val list = maList.slice(0, len)
      val today = list.head
      val isIncre = CloseIncreaseUtil.comIncrea(list) //3个交易日递增
      if (comp(today) //今日MA5>MA20>MA30
        && isIncre //最近3天收盘价递增
        && new BigDecimal(today.getStockDayVo.getTurnoverRatio).compareTo(new BigDecimal(4.5/100)) > 0 //换手率要大于4.5%
        && new BigDecimal(today.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(2/100)) > 0 //涨幅大于3%
      ) {
        val filterList = list.filter(comp(_))
        isOK = list.size != filterList.size
      }
    }
  }

  private def comp(v0: StockMaVo): Boolean = {
    val ma5 = new BigDecimal(v0.getMa5)
    val ma10 = new BigDecimal(v0.getMa10)
    val ma20 = new BigDecimal(v0.getMa20)
    val ma30 = new BigDecimal(v0.getMa30)
    ma5.compareTo(ma20) > 0
      && ma20.compareTo(ma30) > 0
  }

}
