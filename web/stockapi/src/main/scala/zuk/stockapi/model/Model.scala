package zuk.stockapi.model

trait Model {
  def isHit: Boolean //复合策略
  def run: Unit //运行模型
}
