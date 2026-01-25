package zuk.tu_share.pass

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.DataFrame
import zuk.tu_share.backtest.BackTest
import zuk.tu_share.dto.{ModuleDay, TsStock}
import zuk.tu_share.module.{IModel, MA3_0_Model, MA3_1_Model, MA3_2_Model, MA3_3_Model, MA1_Model}
import zuk.tu_share.utils.LicenseUtil
import zuk.utils.SendMail

import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import zuk.tu_share.CammandParam

object PassFactory {

  private def moduleList(): List[IModel] = {
    List(
        new MA1_Model,
        new MA3_0_Model,
        new MA3_1_Model,
        new MA3_2_Model,
        new MA3_3_Model
    )
  }

  private def passList(): List[IPass] = {
    List(
      new PassMA,
      new PassPriceLimit
    )
  }

  /***
   * 模型分析
   * @param map key是股票代码， list是分析数据
   * @param backtestLenght 回测数据长度
   * @return
   */
  def doModule(map: mutable.HashMap[String, List[ModuleDay]], backtestLenght: Int = 0) = {

    val finishModules = ListBuffer[IModel]()
    var count = 0
    map.foreach(e=>{
      val modules = moduleList()
      modules.foreach(module=>{
        try {
          val stock = e._1
          //        val moduleDayList = e._2
          val moduleDayList = e._2.slice(backtestLenght, e._2.size) //取前几个交易日的数据，用于回测
          if (backtestLenght > 0) {
            //如果回测
            var startIndex = backtestLenght - 3
            if (startIndex < 0) {
              startIndex = backtestLenght - 2
            }
            if (startIndex < 0) {
              startIndex = backtestLenght - 1
            }
            module.sells ++= e._2.slice(startIndex, backtestLenght).reverse //连续两天
            module.buy = e._2(backtestLenght) //购买
          }
          doPass(moduleDayList)
          module.run(moduleDayList)
          count = count + 1
          println(s"mod:${CammandParam.param.back.toString.substring(0,1)}:${count}/${map.size * modules.size}")
        }
        catch
          case exception: Exception => exception.printStackTrace()
      })
      finishModules ++= modules
    })

    println("完成模型分析")
    val filterModules = finishModules.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null)

    //todo 多模型推荐
    var moreModuleStr = filterModules.map(_.getStockDto().tsStock).groupBy(_.ts_code).filter(_._2.size>1).map(_._2.head).map(e=>{
      val href = e.getEastmoneyURL()
      val name_href = s"<a href=\"${href}\">" + e.name + "</a>"
      e.ts_code + ", " + name_href
    }).mkString("<br><br>\n\n")
    moreModuleStr = "多模型推荐<br><br>\n\n" + moreModuleStr

    //todo 各模型推荐
    var singleModuleStr = filterModules.groupBy(_.getClass.getSimpleName).filter(_._2.size>0).toList.sortBy(_._2.head.winRate).reverse.map(tp2=>{
      val moduleName = tp2._1
      val moduleList = tp2._2

      BackTest.backTestList ++= moduleList  //收集回测数据

      val stockDtos = moduleList.map(_.getStockDto())

      if(LicenseUtil.check()){
        //如果许可通过，则打印出结果
        println(moduleName)
        println(stockDtos.map(_.tsStock).map(e => s"${e.ts_code}, ${e.name}").mkString("\n"))
      }

      var htmlContent = stockDtos.toList.sortBy(_.turnoverRate).reverse.map(dto => {
        val e = dto.tsStock
        val href = e.getEastmoneyURL()
        val name_href = s"<a href=\"${href}\">" + e.name + "</a>"
        s"${e.ts_code}, ${name_href}, ${e.area}，${e.industry}, ${dto.limitUp}, ${dto.limitDown}, ${dto.turnoverRate}活跃"
      }).mkString("\n<br><br>\n")

      htmlContent =  s"【${moduleList.head.winRate}】${moduleList.head.desc()}, ${moduleList.head.getClass.getSimpleName}<br><br>" + htmlContent

      htmlContent
    }).mkString("<br><br>\n\n")
    singleModuleStr = "单模型推荐<br><br>\n\n" +  singleModuleStr

    if(backtestLenght==0 && LicenseUtil.check()){
      sendMail(moreModuleStr + "<br><br>\n\n" + singleModuleStr)
    }

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

}
