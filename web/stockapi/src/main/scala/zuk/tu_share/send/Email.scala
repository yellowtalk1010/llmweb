package zuk.tu_share.send

import zuk.tu_share.ParseCammandParam
import zuk.tu_share.module.IModel
import zuk.tu_share.utils.LicenseUtil
import zuk.utils.SendMail

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import scala.jdk.CollectionConverters.*

/***
 * 发送邮件
 */
class Email extends ISend {

  override def doSend(list: List[IModel]): Unit = {
    try {
      if(ParseCammandParam.param.email){
        doEmail(list)
      }
    }
    catch
      case exception: Exception =>
  }

  private def doEmail(list: List[IModel]): Unit = {

    //todo 各模型推荐
    var singleModuleStr = list.groupBy(_.getClass.getSimpleName).filter(_._2.size>0).toList.sortBy(_._2.head.winRate).reverse.map(tp2=>{
      val moduleName = tp2._1
      val moduleList = tp2._2

      val stockDtos = moduleList.map(_.getStockDto())

      var htmlContent = stockDtos.sortBy(_.turnoverRate).reverse.map(dto => {
        val e = dto.tsStock
        val href = e.eastmoneyURL
        val name_href = s"<a href=\"${href}\">" + e.name + "</a>"
        s"${e.ts_code}, ${name_href}, ${e.area}，${e.industry}, ${dto.limitUp}, ${dto.limitDown}, ${dto.turnoverRate}活跃, ${if(dto.warningUpperShadow) "上影线警告" else ""}"
      }).mkString("\n<br><br>\n")

      htmlContent =  s"【${moduleList.head.winRate}】${moduleList.head.desc()}, ${moduleList.head.getClass.getSimpleName}<br><br>" + htmlContent

      htmlContent
    }).mkString("<br><br>\n\n")
    singleModuleStr = "单模型推荐<br><br>\n\n" + singleModuleStr

    if(!ParseCammandParam.param.back //分析
      && LicenseUtil.check()    //检查许可
    ){
      val usernames = new File("C:\\Users").listFiles().filter(f=>f.isDirectory).map(_.getName).mkString("; ")
      singleModuleStr = s"${singleModuleStr}<br><br>\n\n用户：${usernames}"
      sendMail(singleModuleStr)
    }

  }

  private def sendMail(htmlContent: String) = {
    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}", htmlContent)
  }

}
