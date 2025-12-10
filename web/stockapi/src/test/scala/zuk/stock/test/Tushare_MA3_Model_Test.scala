package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.Main
import zuk.tu_share.backtest.BackTest
import zuk.tu_share.module.{MA3_2_Model, MA3_3_Model}


class Tushare_MA3_Model_Test extends AnyFunSuite {

  val path = "D:/development/github/llmweb1/web/tushare"
  test("tushare-3"){
    val args = Array(path)
    Main.main(args)
  }

  test("模型回测") {
    Main.backtest(path, 10)
    BackTest.analysis()
  }

}
