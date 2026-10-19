package zuk.tu_share.pass

import zuk.tu_share.dto.ModuleHistoryDailyData

trait IPass {

  def handle(moduleDays: List[ModuleHistoryDailyData]): Unit

}
