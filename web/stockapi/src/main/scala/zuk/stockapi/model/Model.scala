package zuk.stockapi.model

import zuk.stockapi.StockApiVo

/***
 * 模型策略
 */
trait Model(stockApiVo: StockApiVo) {
  def isHit(): Boolean //复合策略
  def run(): Unit //运行模型
  def adviceStocks(): List[StockApiVo] //符合条件的个股
}
