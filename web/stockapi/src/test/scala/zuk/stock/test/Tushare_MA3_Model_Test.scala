package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.Main


class Tushare_MA3_Model_Test extends AnyFunSuite {

  val path = "D:/development/github/llmweb1/web/tushare"
  test("tushare-3"){
    val args = Array(path)
    Main.main(args)
  }

  test("模型回测") {
    for(i <- 1 until 20) {
      Main.backtest(path, i)
      println()
    }

  }

}
