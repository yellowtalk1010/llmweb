package zuk

import zuk.tu_share.backtest.BackTest
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
    for (i <- 0 until args.size) {
      val v = args(i).toLowerCase
      v match
        case "-path" =>
          CammandParam.param.path = args(i+1)
        case "-pwd" =>
          CammandParam.param.pwd = args(i+1)
        case "-back" =>
          CammandParam.param.back = true
        case "-back_step" =>
          CammandParam.param.back_step = args(i+1).toInt
        case "-json" =>
          CammandParam.param.json = true
        case _=>
    }
     
    if(!LicenseUtil.checkPwd()){
      println("pwd err")
      System.exit(0)
    }
    println(s"path:${CammandParam.param.toString}")
    if (!CammandParam.param.back){
      val map = DataFrame.load(CammandParam.param.path)
      PassFactory.doModule(map)
    }
    else {
      //回测
      backtest(CammandParam.param.path, CammandParam.param.back_step)
      BackTest.analysis()
    }


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
    }
  }

}
