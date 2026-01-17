package zuk

import zuk.tu_share.{CammandParam, DataFrame}
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.utils.LicenseUtil

import java.io.PrintStream
import java.nio.charset.{Charset, StandardCharsets}
import java.util.Locale

object Main {

  def main(args: Array[String]): Unit = {

    // 设置默认编码
    fixWindowsConsole()

    if(args==null || args.size==0) {
      println("path is empty.")
    }
    else {
      for (i <- 0 until args.size) {
        val v = args(i).toLowerCase
        v match
          case "-path" =>
            CammandParam.param.path = args(i+1)
          case "-pwd" =>
            CammandParam.param.pwd = args(i+1)
          case _=>
      }
    }
    if(!LicenseUtil.checkPwd()){
      println("password error.")
      System.exit(0)
    }
    println(s"path:${CammandParam.param.toString}")
    val map = DataFrame.load(CammandParam.param.path)
    PassFactory.doModule(map)

  }

  def backtest(path: String, days: Int): Unit = {
    val map = DataFrame.load(path)

    for(i <- 1 to days) {
      PassFactory.doModule(map,i)
      println(s">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${i}")
    }

  }

  private def fixWindowsConsole(): Unit = {
    try {
      // Windows 控制台默认是 GBK/GB2312
      val encoding = "UTF-8"
      System.setProperty("file.encoding", encoding)
      // 获取系统默认编码
      val defaultCharset = Charset.forName(encoding)
      System.setOut(new PrintStream(System.out, true, defaultCharset))
      System.setErr(new PrintStream(System.err, true, defaultCharset))
      // 设置中文 locale
      Locale.setDefault(Locale.CHINA)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

}
