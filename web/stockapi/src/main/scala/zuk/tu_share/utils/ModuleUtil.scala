package zuk.tu_share.utils

import scala.jdk.CollectionConverters.*

object ModuleUtil {

  def handule(): Unit = {
    AllStockUtil.loadData()
    HmDetailUtil.loadData()
    TopInstUtil.loadData()
    val codeSet = HmDetailUtil.hmDetailMap.flatMap(_._2.map(e=>s"${e.ts_code},${e.ts_name}")).toSet
    println("总票数:" + codeSet.size)
    codeSet.foreach(println)

  }


  def main(args: Array[String]): Unit = {
    handule()
  }

}
