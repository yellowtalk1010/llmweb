package zuk.tu_share.utils

import scala.jdk.CollectionConverters.*

object ModuleUtil {

  def handule(): Unit = {
    val codeSet = HmDetailUtil.loadData().flatMap(_._2.map(e=>s"${e.ts_code},${e.ts_name}")).toSet
    println("总票数:" + codeSet.size)
    AllStockUtil.loadData().filter(e=>codeSet.contains(e.ts_code)).map(e=>s"${e.ts_code},${e.name},${e.area},${e.industry}")

  }


  def main(args: Array[String]): Unit = {
    handule()
  }

}
