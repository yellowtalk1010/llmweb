package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math
import java.math.BigDecimal

/***
 * 连续3天涨停
 */
class MA4_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  private var isOK: Boolean = false

  override def isHit(): Boolean = isOK

  override def run(): Unit = {
    val len = 4
    if(maList.size>=len + 1){
      val takelist = maList.take(len)

      val list1 = takelist.filter(e=>{
        val changeRatio = e.getStockDayVo.getChangeRatio
        new math.BigDecimal(changeRatio).compareTo(new math.BigDecimal(6.0))>=0
      })

      val list2 = takelist.filter(e => {
        val changeRatio = e.getStockDayVo.getChangeRatio
        new math.BigDecimal(changeRatio).compareTo(new math.BigDecimal(2.0)) >= 0
      })

      //
      var times = 0
      for (i <- 0 until len) {
        val today = maList(i)
        val yesterday = maList(i + 1)
        if(new BigDecimal(today.getStockDayVo.getClose).compareTo(new BigDecimal(yesterday.getStockDayVo.getClose))>0){
          times = times + 1
        }
        else {
//          println(".")
        }
      }


      if(list1.size >= 2
        && list2.size >= 3
        && times >= 4
      ){
        isOK = true
      }

    }
  }

}
