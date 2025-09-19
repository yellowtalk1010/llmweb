package zuk.stockapi.model

import zuk.stockapi.StockMaVo

class AVG_Model(maList: List[StockMaVo]) extends Model {

  override def isHit(): Boolean = false

  override def run(): Unit = {

  }
}
