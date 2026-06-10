package zuk

import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {

  test("main推荐测试"){
    val args = Array("-path", "D:\\development\\github\\stockapi\\", "-json")
    zuk.Main.main(args)
  }

  test("main回测测试") {
    val args = Array("-path", "D:\\development\\github\\stockapi\\", "-json", "-back")
    zuk.Main.main(args)
  }

}
