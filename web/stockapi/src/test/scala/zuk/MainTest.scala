package zuk

import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {

  test("main方法测试"){
    val args = Array("-path", "D:\\development\\github\\stockapi\\", "-json")
    zuk.Main.main(args)
  }

}
