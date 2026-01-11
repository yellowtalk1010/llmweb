package zuk.tu_share.utils

import java.text.SimpleDateFormat
import java.util.Date

object License {

  def check(): Boolean = {
    try {
      val start = 20260111
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

  def check(pwd: String): Boolean = {
    pwd.toLowerCase.equals("ilovehuangliao".toLowerCase) && check()
  }

}
