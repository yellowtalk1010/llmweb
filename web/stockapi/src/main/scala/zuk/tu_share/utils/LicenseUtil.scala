package zuk.tu_share.utils

import zuk.tu_share.CammandParam

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object LicenseUtil {

  def checkDate(): Boolean = {
    try {
      val start = 20260222
      val cur = new SimpleDateFormat("yyyyMMdd").format(new Date()).toInt
      val end = 20260630
      val st = start <= cur && cur <= end
      if (st) {
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

  def checkPwd(): Boolean = {
    val userNameFile = new File("C:\\Users\\5132")
    CammandParam.param.pwd.toLowerCase.equals("huangliaofather".toLowerCase)
    || userNameFile.getName.toLowerCase.equals("5132")
  }

  def check(): Boolean = {
    checkPwd() && checkDate()
  }

}
