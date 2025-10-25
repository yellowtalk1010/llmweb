package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math
import java.math.BigDecimal

/***
 * 周帆低吸策略
 */
class MA6_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  private var isOK: Boolean = false

  override def isHit(): Boolean = isOK

  override def run(): Unit = {
    if(maList.size>5){
      val takelist = maList.take(5)

      val list1 = takelist.filter(e=>{
        new BigDecimal(e.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(8.0)) > 0
      })

      val list2 = takelist.filter(e=>{
        new BigDecimal(e.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(-8.0)) < 0
      })

      if (list1.size >= 2 && list2.size >= 2){
        isOK = true
      }

    }
  }


}
