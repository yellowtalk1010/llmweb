package zuk.tu_share.pass

import zuk.tu_share.dto.ModuleDay

trait IPass {

  def handle(moduleDays: List[ModuleDay]): List[ModuleDay]

}
