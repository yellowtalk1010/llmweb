package zuk.stock.test

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.Main
import zuk.tu_share.backtest.BackTest
import zuk.tu_share.module.HM_MOD.handule
import zuk.tu_share.module.{HM_MOD, MA3_2_Model, MA3_3_Model}

import java.io.File


class Tushare_MA3_0_Model_Test extends AnyFunSuite {

  val path = "tushare"
  test("tushare-3"){
    val args = Array(path)
    Main.main(args)
  }

  test("模型回测") {
    Main.backtest(path, 180)
    BackTest.analysis()
  }

  test("龙虎榜复盘") {
    HM_MOD.handule()
  }

  test("计算") {
    var bj = 200000.0d
    for(i <- 1 to 12) {
      println(s"${i}月，${bj}")
      for(j <- 1 to 20) {
        bj = bj + (bj * 0.01)
        println(s"   第${j}天，${bj}")
      }

    }
  }

  test("copy"){
    val path = "D:\\development\\github\\llmweb\\web\\tushare\\daily\\"
    val dir = new File(path)
    val dirFiles = dir.listFiles()
    dirFiles.filter(_.isDirectory).foreach(df=>{

      val file2024 = new File(df.getAbsolutePath + File.separator + "2024.csv")
      println(file2024.getAbsolutePath)
      FileUtils.copyFile(new File("D:\\development\\github\\llmweb\\web\\stockapi\\src\\test\\scala\\zuk\\stock\\test\\2024.csv"), file2024)
    })
  }

}
