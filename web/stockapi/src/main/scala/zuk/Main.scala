package zuk

import zuk.run.{DataFrame, RunModule3}

object Main {

  def main(args: Array[String]): Unit = {
    if(args==null || args.size==0) {
      println("path is empty.")
      System.exit(1)
    }
    else {
      val path = args(0)
      println(s"path:${path}")
//      RunModule3.handle(path)
      DataFrame.load(path)
    }
  }

}
