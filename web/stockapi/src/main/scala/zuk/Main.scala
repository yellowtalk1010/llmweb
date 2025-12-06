package zuk

import zuk.run.{DataFrame, RunModule3}
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
//      RunModule3.handle(path)
      val map = DataFrame.load(path)
      map.foreach(e=>{
        PassFactory.doModule(e._2)
      })
    }
  }

}
