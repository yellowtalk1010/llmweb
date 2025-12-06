package zuk.tu_share.pass

import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.module.{IModel, MA3_1_Model, MA3_Model}

object PassFactory {

  def doModule(moduleDays: List[ModuleDay]) = {
    doPass(moduleDays)
    moduleList().foreach(module=>{
      module.run(moduleDays)
      module
    })
  }

  private def moduleList(): List[IModel] = {
    List(new MA3_Model,
      new MA3_1_Model
    )
  }

  private def doPass(moduleDays: List[ModuleDay]) = {
    passList().foreach(pass=>{
      pass.handle(moduleDays)
    })
  }

  private def passList(): List[IPass] = {
    List(new PassMA)
  }

}
