package zuk

import zuk.tu_share.backtest.BackTest
import zuk.tu_share.{ParseCammandParam, DataFrame}
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.utils.LicenseUtil

import java.io.PrintStream
import java.nio.charset.{Charset, StandardCharsets}
import java.util.Locale

object Main {

  def main(args: Array[String]): Unit = {

    // 设置默认编码
    fixWindowsConsole()
    //参数解析
    ParseCammandParam.parse(args)
    println(s"path:${ParseCammandParam.param.toString}")
    //许可密码校验
    if(!LicenseUtil.checkPwd()){
      println("pwd err")
      System.exit(0)
    }
    if (ParseCammandParam.param.back){
      //回测
      val map = DataFrame.load(ParseCammandParam.param.path)
      for (i <- 0 to ParseCammandParam.param.back_step) {
        PassFactory.doModule(map, i)
        println(s">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${i}")
      }

      BackTest.analysis()
    }
    else {
      //分析
      val map = DataFrame.load(ParseCammandParam.param.path)
      PassFactory.doModule(map)
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
    }
  }

}
