package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}

class AVG_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  override def isHit(): Boolean = false

  override def run(): Unit = {

  }
}
