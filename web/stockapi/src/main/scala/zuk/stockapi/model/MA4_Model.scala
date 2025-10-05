package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math
import java.math.BigDecimal

/***
 * 第二天 ma5 上生 ma10
 */
class MA4_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  private var isOK: Boolean = false

  override def isHit(): Boolean = isOK

  override def run(): Unit = {
    if(maList.size>=3){
      val list = maList.take(3)
      val head = list.head
      if (
        new BigDecimal(list(0).getMa5).compareTo(new BigDecimal(list(0).getMa10)) >= 0
          && new BigDecimal(list(1).getMa5).compareTo(new BigDecimal(list(1).getMa10)) >= 0
          && new BigDecimal(list(2).getMa5).compareTo(new BigDecimal(list(2).getMa10)) <= 0
          && new BigDecimal(head.getStockDayVo.getTurnoverRatio).compareTo(new BigDecimal(4)) >= 0 //换手率
          && new BigDecimal(head.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(4)) >= 0 //涨幅度
      ) {
        isOK = true
      }
    }
  }

}
