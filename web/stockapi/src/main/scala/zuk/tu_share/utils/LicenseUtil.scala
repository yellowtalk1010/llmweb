package zuk.tu_share.utils

import zuk.tu_share.CammandParam

import java.text.SimpleDateFormat
import java.util.Date

object LicenseUtil {

  def checkDate(): Boolean = {
    try {
      val start = 20260205
      val cur = new SimpleDateFormat("yyyyMMdd").format(new Date()).toInt
      val end = 20260216
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
    CammandParam.param.pwd.toLowerCase.equals("huangliaofather".toLowerCase)
  }

  def check(): Boolean = {
    checkPwd() && checkDate()
  }

}
