package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}
import java.math.BigDecimal

/***
 * ma5 上生 ma10
 */
class MA3_Model (stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  private var isOK: Boolean = false

  override def isHit(): Boolean = isOK

  override def run(): Unit = {
    val list = maList.take(3)

    if((new BigDecimal(list(0).getMa5).compareTo(new BigDecimal(list(0).getMa10)) >= 0
      && new BigDecimal(list(1).getMa5).compareTo(new BigDecimal(list(1).getMa10)) <= 0
      && new BigDecimal(list(2).getMa5).compareTo(new BigDecimal(list(2).getMa10)) <= 0)
      || (new BigDecimal(list(0).getMa5).compareTo(new BigDecimal(list(0).getMa10)) >= 0
        && new BigDecimal(list(1).getMa5).compareTo(new BigDecimal(list(1).getMa10)) >= 0
        && new BigDecimal(list(2).getMa5).compareTo(new BigDecimal(list(2).getMa10)) <= 0)
    ){
      isOK = true
    }
  }

}
