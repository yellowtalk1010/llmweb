package zuk.tu_share.module

import zuk.tu_share.dto.ModuleDay

trait IModel {
  def isHit(): Boolean //复合策略
  def run(days: List[ModuleDay]): Unit //运行模型
}
