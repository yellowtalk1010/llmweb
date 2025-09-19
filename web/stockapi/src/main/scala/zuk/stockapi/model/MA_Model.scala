package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math.BigDecimal
/***
 * MA连续3天上升
 */
class MA_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  var isOK: Boolean = false

  override def isHit(): Boolean = this.isOK

  override def run(): Unit = {
    val list = maList.slice(0,3)
    val filterList = list.filter(v0=>{
      val ma5 = new BigDecimal(v0.getMa5)
      val ma10 = new BigDecimal(v0.getMa10)
      val ma20 = new BigDecimal(v0.getMa20)
      val ma30 = new BigDecimal(v0.getMa30)
      val st = ma5.compareTo(ma10) > 0 && ma10.compareTo(ma20) > 0 && ma20.compareTo(ma30) > 0
      st
    })
    this.isOK = list.size==filterList.size
  }

}
