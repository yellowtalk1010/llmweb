package zuk.tu_share.pass

import zuk.tu_share.dto.ModuleDay

object PassFactory {

  def doPass(moduleDays: List[ModuleDay]) = {
    passList().foreach(pass=>{
      pass.handle(moduleDays)
    })
  }

  def passList(): List[IPass] = {
    List(new PassMA)
  }

}
