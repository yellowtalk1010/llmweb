package zuk.tu_share.module

import zuk.tu_share.dto.TsStock
import zuk.tu_share.utils.{AllStockUtil, EastMoneyUtil, HmDetailUtil}
import zuk.utils.SendMail

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

/***
 * 龙虎榜模型
 */
object HM_MOD {

  private val lines = ListBuffer[String]()
  private val LINE = "============================================================================"

  def handule(): Unit = {
    val codeSet = HmDetailUtil.loadData().flatMap(_._2.map(e=>s"${e.ts_code}")).toSet
    val hm_stocks = AllStockUtil.loadData().filter(e=>codeSet.contains(e.ts_code))
    doPrintln("总票数:" + hm_stocks.size)
    hm_stocks.map(toStr).foreach(doPrintln)
    doPrintln(s"\n${LINE}行业排序${LINE}")
    val industrySortStocks = hm_stocks.groupBy(_.industry).toList.sortBy(_._2.size).reverse
    industrySortStocks.map(tp=>s"${tp._1},${tp._2.size}").foreach(doPrintln)
    doPrintln(s"\n${LINE}区域排序${LINE}")
    val areaSortStocks = hm_stocks.groupBy(_.area).toList.sortBy(_._2.size).reverse
    areaSortStocks.map(tp=>s"${tp._1},${tp._2.size}").foreach(doPrintln)
    doPrintln(s"\n${LINE}符合行业、区域排名前10交集${LINE}")
    hm_stocks.filter(s=>{
      industrySortStocks.take(10).map(_._1).contains(s.industry)   //行业排名取前5名，前几名，差距不大。
      && areaSortStocks.take(10).map(_._1).contains(s.area)        //地区排名取第1名，因为第一名的权重很大，所以，只取第一名。
    }).map(toStr).foreach(doPrintln)
    doPrintln(s"\n${LINE}参与游资机构数量排名${LINE}")
    val hmJoins = HmDetailUtil.loadData().toList.flatMap(_._2).groupBy(_.ts_code).map(e=>(e._1, e._2.map(_.hm_name).toSet)).toList.sortBy(_._2.size).reverse.map(tp=>{
      val hmNames = tp._2
      val stock = AllStockUtil.loadData().filter(s=>s.ts_code.equals(tp._1)).head
      doPrintln(s"${toStr(stock)}, ${hmNames.size}个, ${hmNames.mkString("; ")}")
      (stock, hmNames)
    })
    doPrintln(s"\n${LINE}过去30个交易日净买卖排序${LINE}")
    val netAmounts = HmDetailUtil.loadData().flatMap(_._2).groupBy(_.ts_code).map(tp=>{
      val stock = AllStockUtil.loadData().filter(_.ts_code.equals(tp._1)).head
      val sum = tp._2.map(_.net_amount.toDouble).sum
      (stock, sum)
    }).toList.sortBy(_._2).reverse
    netAmounts.foreach(tp=>{
      doPrintln(s"${toStr(tp._1)}, ${tp._2}")
    })

    println("发送邮件")
    SendMail.sendSimpleEmail("513283439@qq.com", "513283439@qq.com", s"${new SimpleDateFormat("yyyyMMdd").format(new Date())}龙虎榜复盘", s"${lines.mkString("<br>")}")
  }

  private def toStr(stock: TsStock): String = {
    s"${stock.ts_code},${stock.name},${stock.area},${stock.industry},${EastMoneyUtil.createURL(stock)}"
  }

  private def doPrintln(line: String): Unit = {
    lines += line
    println(line)
  }

  def main(args: Array[String]): Unit = {
    handule()
  }

}
