package zuk.tu_share.module

trait IModel {
  def isHit(): Boolean //复合策略
  def run(): Unit //运行模型
}
