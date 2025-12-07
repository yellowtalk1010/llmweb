package zuk.tu_share.pass

import zuk.tu_share.DataFrame
import zuk.tu_share.dto.{ModuleDay, TsStock}
import zuk.tu_share.module.{IModel, MA3_1_Model, MA3_2_Model, MA3_Model}
import zuk.utils.SendMail

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable

object PassFactory {

  def doModule(map: mutable.HashMap[String, List[ModuleDay]], backtestLenght: Int = 0) = {

    val modules = moduleList()
    var count = 0
    modules.foreach(module=>{
      map.foreach(e=>{
        val stock = e._1
//        val moduleDayList = e._2
        val moduleDayList = e._2.slice(backtestLenght, e._2.size)  //取前几个交易日的数据，用于回测
        if(backtestLenght>0){
          module.backTestTargetList += e._2.slice(backtestLenght-1, backtestLenght)
        }
        doPass(moduleDayList)
        module.run(moduleDayList)
        count = count + 1
        println(s"mod:${count}/${map.size * modules.size}")
      })
    })

    println("完成模型分析")
    modules.filter(e=>e.getTsStocks()!=null && e.getTsStocks().size>0).foreach(mod=>{
      //输出模型分析结论
      val clsName = mod.getClass.getSimpleName
      val stocks = mod.getTsStocks().map(e=>{
        val ls = DataFrame.STOCKS.filter(_.ts_code.equals(e))
        if(ls.size>0){
          Some(ls.head)
        }
        else {
          Option.empty
        }
      }).filter(_.nonEmpty).map(_.get).filter(e=> !e.name.contains("ST"))

      println(clsName)
      println(stocks.map(e => s"${e.ts_code}, ${e.name}").mkString("\n"))

      if(backtestLenght==0){
        //非回测操作，则发送邮件
        sendMail(clsName, stocks)
      }
      else {
        //回测，计算回测胜率效果
        mod.backTestTargetList.map(e=>{
          s"${e.getClass.getSimpleName}, ${e.ts_code}, ${e.name}, ${e.turnover_rate}, ${e.change}"
        })
      }
    })
  }

  private def sendMail(moduleName: String, list: List[TsStock]) = {

    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyyMMdd").format(new Date)

    val htmlContent = list.map(e=>{
      val splits = e.ts_code.split("\\.")
      val href = s"https://quote.eastmoney.com/${splits(1)}${splits(0)}.html"
//      println(href)
      val name_href = s"<a href=\"${href}\">" + e.name + "</a>"
//      println(name_href)
      s"${e.ts_code}，${name_href}，${e.area}，${e.industry}"
    }).mkString("\n<br><br>\n")

    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${moduleName}", htmlContent)
  }

  private def moduleList(): List[IModel] = {
    List(new MA3_Model,
      new MA3_1_Model,
      new MA3_2_Model
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
