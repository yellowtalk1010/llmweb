package zuk.tu_share.utils

import zuk.tu_share.dto.TsStock

import scala.jdk.CollectionConverters.*

object ModuleUtil {

  private val LINE = "============================================================================"

  def handule(): Unit = {
    val codeSet = HmDetailUtil.loadData().flatMap(_._2.map(e=>s"${e.ts_code}")).toSet
    val stocks = AllStockUtil.loadData().filter(e=>codeSet.contains(e.ts_code))
    println("总票数:" + stocks.size)
    stocks.map(toStr).foreach(println)
    println(s"\n${LINE}行业排序${LINE}")
    val industrySortStocks = stocks.groupBy(_.industry).toList.sortBy(_._2.size).reverse
    industrySortStocks.map(tp=>s"${tp._1},${tp._2.size}").foreach(println)
    println(s"\n${LINE}区域排序${LINE}")
    val areaSortStocks = stocks.groupBy(_.area).toList.sortBy(_._2.size).reverse
    areaSortStocks.map(tp=>s"${tp._1},${tp._2.size}").foreach(println)
    println(s"\n${LINE}符合行业、区域排名前10交集${LINE}")
    stocks.filter(s=>{
      industrySortStocks.take(10).map(_._1).contains(s.industry)   //行业排名取前5名，前几名，差距不大。
      && areaSortStocks.take(10).map(_._1).contains(s.area)        //地区排名取第1名，因为第一名的权重很大，所以，只取第一名。
    }).map(toStr).foreach(println)
    println(s"\n${LINE}参与游资机构数量排名${LINE}")
    val hmJoins = HmDetailUtil.loadData().toList.flatMap(_._2).groupBy(_.ts_code).map(e=>(e._1, e._2.map(_.hm_name).toSet)).toList.sortBy(_._2.size).reverse.map(tp=>{
      val hmNames = tp._2
      val stock = AllStockUtil.loadData().filter(s=>s.ts_code.equals(tp._1)).head
      println(s"${toStr(stock)}, ${hmNames.size}个, ${hmNames.mkString("; ")}")
      (stock, hmNames)
    })


  }

  private def toStr(stock: TsStock): String = s"${stock.ts_code},${stock.name},${stock.area},${stock.industry},${EastMoneyUtil.createURL(stock)}"




  def main(args: Array[String]): Unit = {
    handule()
  }

}
