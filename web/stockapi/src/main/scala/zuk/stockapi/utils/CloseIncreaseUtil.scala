package zuk.stockapi.utils

import zuk.stockapi.StockMaVo
import java.math.BigDecimal

object CloseIncreaseUtil {

  /**
   * 收盘价是否递增
   */
  def closePriceIncrea(list: List[StockMaVo]): Boolean = {
    var st = true
    for (i <- 0 until list.size if st) {
      val e0 = list(i)
      val ls = list.splitAt(i)._2
      val filter = ls.filter(e => {
        new BigDecimal(e0.getStockDayVo.getClose).compareTo(new BigDecimal(e.getStockDayVo.getClose)) >= 0
      })
      st = ls.size == filter.size
    }
    st
  }

}
