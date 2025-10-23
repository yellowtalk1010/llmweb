package zuk.jiucai.test

import org.scalatest.funsuite.AnyFunSuite

class JiucaiTest extends AnyFunSuite {

  test("config"){
    val list = List()
    val c1 = zuk.jiucai.test.Config.createConfigJsonFile(list)
  }

}
