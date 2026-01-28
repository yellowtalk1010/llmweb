package zuk.tu_share.send

import zuk.tu_share.CammandParam
import zuk.tu_share.backtest.BackTest
import zuk.tu_share.dto.RecommendResult
import zuk.tu_share.module.IModel
import zuk.tu_share.utils.LicenseUtil
import zuk.utils.SendMail

import java.text.SimpleDateFormat
import java.util.Date
import scala.jdk.CollectionConverters.*

/***
 * 发送邮件
 */
class Email extends ISend {

  override def doSend(list: List[IModel]): Unit = {
    try {
      doEmail(list)
    }
    catch
      case exception: Exception =>
  }

  private def doEmail(list: List[IModel]): Unit = {

    //todo 多模型推荐
    var moreModuleStr = list.map(_.getStockDto().tsStock).groupBy(_.ts_code).filter(_._2.size > 1).map(_._2.head).map(e => {
      val href = e.getEastmoneyURL()
      val name_href = s"<a href=\"${href}\">" + e.name + "</a>"
      e.ts_code + ", " + name_href
    }).mkString("<br><br>\n\n")
    moreModuleStr = "多模型推荐<br><br>\n\n" + moreModuleStr


    //todo 各模型推荐
    var singleModuleStr = list.groupBy(_.getClass.getSimpleName).filter(_._2.size>0).toList.sortBy(_._2.head.winRate).reverse.map(tp2=>{
      val moduleName = tp2._1
      val moduleList = tp2._2

      BackTest.backTestList ++= moduleList  //收集回测数据

      val stockDtos = moduleList.map(_.getStockDto())

      if(LicenseUtil.check()){
        //如果许可通过，则打印出结果
        println(moduleName)
        println(stockDtos.map(_.tsStock).map(e => s"${e.ts_code}, ${e.name}").mkString("\n"))

        if(!CammandParam.param.back && CammandParam.param.json){
          RecommendResult.results.addAll(stockDtos.asJava) //收集全部数据
        }

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
    singleModuleStr = "单模型推荐<br><br>\n\n" + singleModuleStr

    if(!CammandParam.param.back //分析
      && LicenseUtil.check()    //检查许可
    ){
      sendMail(moreModuleStr + "<br><br>\n\n" + singleModuleStr)
    }

  }

  private def sendMail(htmlContent: String) = {
    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}", htmlContent)
  }

}
