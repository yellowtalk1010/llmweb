package zuk

import zuk.tu_share.DataFrame
import zuk.tu_share.pass.PassFactory

object Main {

  def main(args: Array[String]): Unit = {
    if(args==null || args.size==0) {
      println("path is empty.")
      System.exit(1)
    }
    else {
      val path = args(0)
      println(s"path:${path}")
      val map = DataFrame.load(path)
      PassFactory.doModule(map)
    }
  }

  def backtest(path: String, days: Int): Unit = {
    val map = DataFrame.load(path)

    for(i <- 2 to days) {
      PassFactory.doModule(map,i)
      println(s">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${i}")
    }

  }

}
