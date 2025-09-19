package zuk.stockapi.model

import zuk.stockapi.StockMaVo

/***
 * MA模型开发
 */
class MA_Model(maList: List[StockMaVo]) extends Model {

  var isOK: Boolean = false

  override def isHit(): Boolean = this.isOK
  override def run(): Unit = {
    val list = maList.take(3)
    println()
  }

}
