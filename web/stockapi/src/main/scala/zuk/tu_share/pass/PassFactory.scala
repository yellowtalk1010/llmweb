package zuk.tu_share.pass

import zuk.tu_share.DataFrame
import zuk.tu_share.backtest.BackTest
import zuk.tu_share.dto.{ModuleDay, TsStock}
import zuk.tu_share.module.{IModel, MA3_1_Model, MA3_2_Model, MA3_3_Model, MA3_Model}
import zuk.utils.SendMail

import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object PassFactory {

  def doModule(map: mutable.HashMap[String, List[ModuleDay]], backtestLenght: Int = 0) = {

    val finishModules = ListBuffer[IModel]()
    var count = 0
    map.foreach(e=>{
      val modules = moduleList()
      modules.foreach(module=>{
        val stock = e._1
        //        val moduleDayList = e._2
        val moduleDayList = e._2.slice(backtestLenght, e._2.size) //取前几个交易日的数据，用于回测
        if (backtestLenght > 0) {
          var startIndex = backtestLenght - 3
          if (startIndex < 0) {
            startIndex = backtestLenght - 1
          }
          module.sells ++= e._2.slice(startIndex, backtestLenght).reverse //连续两天
          module.buy = e._2(backtestLenght)
        }
        doPass(moduleDayList)
        module.run(moduleDayList)
        count = count + 1
        println(s"mod:${count}/${map.size * modules.size}")
      })
      finishModules ++= modules
    })

    println("完成模型分析")
    val filterModules = finishModules.filter(e=>e.getTsStocks()!=null && e.getTsStocks().size>0)
    filterModules.groupBy(_.getClass.getSimpleName).foreach(tp2=>{
      val moduleName = tp2._1
      val moduleList = tp2._2

      val stocks = moduleList.flatMap(_.getTsStocks()).map(s=>{
        val ls = DataFrame.STOCKS.filter(_.ts_code.equals(s))
        if(ls.size>0){
          Some(ls.head)
        }
        else {
          Option.empty
        }
      }).filter(_.nonEmpty).map(_.get)
        //.filter(! _.name.contains("ST")) //移除ST股票

      println(moduleName)
      println(stocks.map(e => s"${e.ts_code}, ${e.name}").mkString("\n"))

      if(backtestLenght==0){
        //非回测，则发送邮件
        sendMail(moduleName, stocks.toList)
      }
      else {
        //回测
        BackTest.backTestList ++= moduleList
      }
    })


  }

  private def sendMail(moduleName: String, list: List[TsStock]) = {

    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date)

    val htmlContent = list.map(e=>{
      val splits = e.ts_code.split("\\.")
      val href = s"https://quote.eastmoney.com/${splits(1)}${splits(0)}.html"
//      println(href)
      val name_href = s"<a href=\"${href}\">" + e.name + "</a>"
//      println(name_href)
      s"${e.ts_code}，${name_href}，${e.area}，${e.industry}"
    }).mkString("\n<br><br>\n")

    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate},${moduleName}", htmlContent)
  }

  private def moduleList(): List[IModel] = {
    List(
      //      new MA3_Model,
      //      new MA3_1_Model,
//      new MA3_2_Model,
                new MA3_3_Model
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
