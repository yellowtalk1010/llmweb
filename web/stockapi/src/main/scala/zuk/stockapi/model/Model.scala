package zuk.stockapi.model

import zuk.stockapi.StockApiVo

trait Model(stockApiVo: StockApiVo) {
  def isHit(): Boolean //复合策略
  def run(): Unit //运行模型
}
