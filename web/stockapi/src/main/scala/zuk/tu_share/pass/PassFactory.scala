package zuk.tu_share.pass

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.backtest.BackTest
import zuk.tu_share.dto.{ModuleDay, TsStock}
import zuk.tu_share.module.{IModel, MA3_0_Model, MA3_1_Model, MA3_2_Model, MA3_3_Model}
import zuk.utils.SendMail

import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object PassFactory {

  private def moduleList(): List[IModel] = {
    List(
        new MA3_0_Model,
        new MA3_1_Model,
        new MA3_2_Model,
        new MA3_3_Model
    )
  }

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
            startIndex = backtestLenght - 2
          }
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
    val filterModules = finishModules.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null && StringUtils.isNotBlank(e.getStockDto().tsStock.ts_code))

    val emailContent = filterModules.groupBy(_.getClass.getSimpleName).filter(_._2.size>0).toList.sortBy(_._2.head.winRate).reverse.map(tp2=>{
      val moduleName = tp2._1
      val moduleList = tp2._2

      BackTest.backTestList ++= moduleList  //收集回测数据

      val stockDtos = moduleList.map(_.getStockDto())

      if(license()){
        println(moduleName)
        println(stockDtos.map(_.tsStock).map(e => s"${e.ts_code}, ${e.name}").mkString("\n"))
      }

      var htmlContent = stockDtos.toList.sortBy(_.turnoverRate).reverse.map(dto => {
        val e = dto.tsStock
        val splits = e.ts_code.split("\\.")
        val href = s"https://quote.eastmoney.com/${splits(1)}${splits(0)}.html"
        val name_href = s"<a href=\"${href}\">" + e.name + "</a>"
        s"${e.ts_code}，${name_href}，${e.area}，${e.industry}, ${dto.limitUp}, ${dto.turnoverRate}"
      }).mkString("\n<br><br>\n")

      htmlContent =  s"【${moduleList.head.winRate}】${moduleList.head.desc()}, ${moduleList.head.getClass.getSimpleName}<br><br>" + htmlContent

      htmlContent
    }).mkString("<br><br>\n\n")

    if(backtestLenght==0 && license()){
      sendMail(emailContent)
    }

  }

  private def license(): Boolean = {
    try{
      val start = 20251226
      val cur = new SimpleDateFormat("yyyyMMdd").format(new Date()).toInt
      val end = 20260215
      val st = start <= cur && cur <= end
      if(st){
        //println("OK")
      }
      else {
        //println("OK!")
      }
      st
    }
    catch
      case exception: Exception => false
  }


  private def sendMail(htmlContent: String) = {
    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}", htmlContent)
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
