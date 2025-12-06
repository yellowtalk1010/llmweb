package zuk.tu_share.pass

import zuk.run.DataFrame
import zuk.tu_share.dto.{ModuleDay, TsStock}
import zuk.tu_share.module.{IModel, MA3_1_Model, MA3_Model}
import zuk.utils.SendMail

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable

object PassFactory {

  def doModule(map: mutable.HashMap[String, List[ModuleDay]]) = {

    val modules = moduleList()
    var count = 0
    modules.foreach(module=>{
      map.foreach(e=>{
        val stock = e._1
        val moduleDayList = e._2
        doPass(moduleDayList)
        module.run(moduleDayList)
        count = count + 1
        println(s"moduel:${count}/${map.size * modules.size}")
      })
    })

    println("完成模型分析")
    modules.filter(e=>e.getTsStocks()!=null && e.getTsStocks().size>0).foreach(m=>{
      //输出模型分析结论
      val clsName = m.getClass.getSimpleName
      val stocks = m.getTsStocks().map(e=>{
        val ls = DataFrame.STOCKS.filter(_.ts_code.equals(e))
        if(ls.size>0){
          Some(ls.head)
        }
        else {
          Option.empty
        }
      }).filter(_.nonEmpty).map(_.get)

      sendMail(clsName, stocks)
    })
  }

  private def sendMail(moduleName: String, list: List[TsStock]) = {

    println(moduleName)
    println(list.map(e => s"${e.ts_code}, ${e.name}").mkString("\n"))

    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyyMMdd").format(new Date)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}-推荐 M3", list.map(e=>s"${e.ts_code}，${e.name}，${e.area}，${e.industry}").mkString("\n"))
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
