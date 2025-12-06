package zuk.tu_share.pass

import zuk.run.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.module.{IModel, MA3_1_Model, MA3_Model}

import scala.collection.mutable

object PassFactory {

  def doModule(map: mutable.HashMap[String, List[ModuleDay]]) = {

    val modules = moduleList()
    modules.foreach(module=>{
      map.foreach(e=>{
        val stock = e._1
        val moduleDayList = e._2
        doPass(moduleDayList)
        module.run(moduleDayList)
      })
    })

    println("完成模型分析")
    modules.foreach(m=>{
      val clsName = m.getClass.getSimpleName
      val stocks = m.getTsStocks().map(e=>{
        val ls = DataFrame.STOCKS.filter(_.ts_code.equals(e))
        if(ls.size>0){
          Some(ls.head)
        }
        else {
          Option.empty
        }
      })

      println(clsName)
      println(stocks.filter(_.nonEmpty).map(_.get).map(e=>s"${e.ts_code}, ${e.name}").mkString("\n"))

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
