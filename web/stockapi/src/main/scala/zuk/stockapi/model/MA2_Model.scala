package zuk.stockapi.model

import zuk.stockapi.utils.CloseIncreaseUtil
import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math.BigDecimal
import scala.collection.mutable.ListBuffer

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
      val isIncre = CloseIncreaseUtil.comIncrea(list)
      if (comp(list(0)) && isIncre) {
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
