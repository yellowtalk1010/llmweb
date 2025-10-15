package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math
import java.math.BigDecimal

/***
 * 预测
 */
class MA55_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  private var isOK: Boolean = false

  override def isHit(): Boolean = isOK

  override def run(): Unit = {
    if (maList.size >= 3) {
      val list = maList.take(3)
      val day0 = list(0)
      val day1 = list(1)
      val day2 = list(2)
      if (new BigDecimal(day0.getMa5).compareTo(new BigDecimal(day1.getMa5)) > 0
        && new BigDecimal(day1.getMa5).compareTo(new BigDecimal(day2.getMa5)) > 0 //递增

        && new BigDecimal(day0.getMa10).compareTo(new BigDecimal(day1.getMa10)) < 0
        && new BigDecimal(day1.getMa10).compareTo(new BigDecimal(day2.getMa10)) < 0 //递减

        && new BigDecimal(day0.getMa10).compareTo(new BigDecimal(day0.getMa5)) > 0
        && new BigDecimal(day1.getMa10).compareTo(new BigDecimal(day1.getMa5)) > 0
        //        && new BigDecimal(day2.getMa10).compareTo(new BigDecimal(day2.getMa5)) > 0 //

        && new BigDecimal(day0.getStockDayVo.getTurnoverRatio).compareTo(new BigDecimal(4)) >= 0
        && new BigDecimal(day1.getStockDayVo.getTurnoverRatio).compareTo(new BigDecimal(4)) >= 0 //huan shou
        //
        && new BigDecimal(day0.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(2)) >= 0
        && new BigDecimal(day1.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(2)) >= 0 //zhang fu
      ) {

        if (yuce(maList)) {
          this.isOK = true
        }

      }
    }

  }



  /***
   * shang zhang 3 上升
   * @param maList
   * @return
   */
  private def yuce(maList: List[StockMaVo]): Boolean = {
    val lastDay = maList.head
    val close = lastDay.getStockDayVo.getClose
    val ycj = (new BigDecimal(close).multiply(new BigDecimal("0.02"))).add(new BigDecimal(close))

    val m5list = List(ycj) ++ maList.take(4).map(e=>{
      new BigDecimal(e.getStockDayVo.getClose)
    })
    val new5 = m5list.reduceOption[BigDecimal]((a,b)=>{
      a.add(b)
    }).get.divide(new BigDecimal(5), 5, math.BigDecimal.ROUND_HALF_DOWN)

    val m10list = List(ycj) ++ maList.take(9).map(e => {
      new BigDecimal(e.getStockDayVo.getClose)
    })

    val new10 = m10list.reduceOption[BigDecimal]((a,b)=>{
      a.add(b)
    }).get.divide(new BigDecimal(10), 5, math.BigDecimal.ROUND_HALF_DOWN)

    val st = new5.compareTo(new10) > 0

    st
  }

}
