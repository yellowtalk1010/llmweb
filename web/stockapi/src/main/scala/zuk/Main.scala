package zuk

import zuk.tu_share.DataFrame
import zuk.tu_share.pass.PassFactory

import java.io.PrintStream
import java.nio.charset.{Charset, StandardCharsets}
import java.util.Locale

object Main {

  def main(args: Array[String]): Unit = {

    // 设置默认编码
    fixWindowsConsole()

    var path = "."
    if(args==null || args.size==0) {
      println("path is empty.")
    }
    else {
      path = args(0)
    }

    println(s"path:${path}")
    val map = DataFrame.load(path)
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
