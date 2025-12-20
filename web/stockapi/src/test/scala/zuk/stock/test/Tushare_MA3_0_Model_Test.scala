package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.Main
import zuk.tu_share.backtest.BackTest
import zuk.tu_share.module.HM_MOD.handule
import zuk.tu_share.module.{HM_MOD, MA3_2_Model, MA3_3_Model}


class Tushare_MA3_0_Model_Test extends AnyFunSuite {

  val path = "tushare"
  test("tushare-3"){
    val args = Array(path)
    Main.main(args)
  }

  test("模型回测") {
    Main.backtest(path, 60)
    BackTest.analysis()
  }

  test("龙虎榜复盘") {
    HM_MOD.handule()
  }
}
