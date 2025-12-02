package zuk.stock.test

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.model.{MA3_1_Model, MA3_Model}
import zuk.stockapi.{CalculateMAForDay, CalculateMAForDay_Tushare, LoaderLocalStockData, StockApiVo}

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import scala.jdk.CollectionConverters.*
import org.apache.commons.csv.*
import zuk.utils.SendMail

import java.io.FileReader
import java.io.Reader


class Tushare_MA3_Model_Test extends AnyFunSuite {

  val path = "tushare"
  CalculateMAForDay_Tushare.CSV_PATH = path

  test("tushare-3"){
    //将tushare的csv数据转成对象
    val in = new FileReader(s"${path}\\all_stocks.csv")
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record=>{
      val ts_code = record.get("ts_code")
      val name = record.get("name")
      val splits = ts_code.split("\\.")
      val code = splits(0)
      val jys = splits(1)
      val gl = record.get("industry")
      val stockApiVo = new StockApiVo()
      stockApiVo.setApi_code(code)
      stockApiVo.setName(name)
      stockApiVo.setJys(jys)
      stockApiVo.setGl(gl)
      stockApiVo
    })
//      .filter(e=>e.getApi_code.contains("000753"))
      .toList
    in.close()
    println(s"${codes.size}")

    val tpList = CalculateMAForDay_Tushare.run(codes)

    val socketList = tpList.filter(_._2.size > 0).filter(tp => {
      val stock = tp._1
      val malist = tp._2
      val model = new MA3_Model(stock, malist)
      model.run()
      model.isHit()
    })
      .filter(e=> !e._1.getName.contains("ST")) //排除ST股票

    println(socketList.size)
    socketList.map(e => e._1.getApi_code + "，" + e._1.getName).foreach(println)
    val sdm = new SimpleDateFormat("yyyyMMdd")

    val mailAddress = "513283439@qq.com"
    SendMail.sendSimpleEmail(mailAddress, mailAddress, "推荐 M3", socketList.map(e=>s"${e._1.getApi_code},${e._1.getName}").mkString("\n"))
//    FileUtils.writeLines(new File(s"stockapi/model_result/${sdm.format(new Date)}-MA3.txt"), socketList.map(e => s"${e._1.getApi_code}，${e._1.getName}").asJava)

  }



  test("tushare-3.1"){
    //将tushare的csv数据转成对象
    val in = new FileReader(s"${path}\\all_stocks.csv")
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record=>{
        val ts_code = record.get("ts_code")
        val name = record.get("name")
        val splits = ts_code.split("\\.")
        val code = splits(0)
        val jys = splits(1)
        val gl = record.get("industry")
        val stockApiVo = new StockApiVo()
        stockApiVo.setApi_code(code)
        stockApiVo.setName(name)
        stockApiVo.setJys(jys)
        stockApiVo.setGl(gl)
        stockApiVo
      })
      //      .filter(e=>e.getApi_code.contains("000753"))
      .toList
    in.close()
    println(s"${codes.size}")

    val tpList = CalculateMAForDay_Tushare.run(codes)

    val socketList = tpList.filter(_._2.size > 0).filter(tp => {
        val stock = tp._1
        val malist = tp._2
        val model = new MA3_1_Model(stock, malist)
        model.run()
        model.isHit()
      })
      .filter(e=> !e._1.getName.contains("ST")) //排除ST股票

    println(socketList.size)
    socketList.map(e => e._1.getApi_code + "，" + e._1.getName).foreach(println)
    val sdm = new SimpleDateFormat("yyyyMMdd")

    val mailAddress = "513283439@qq.com"
    SendMail.sendSimpleEmail(mailAddress, mailAddress, "推荐 M3.1", socketList.map(e=>s"${e._1.getApi_code},${e._1.getName}").mkString("\n"))
    //    FileUtils.writeLines(new File(s"stockapi/model_result/${sdm.format(new Date)}-MA3.txt"), socketList.map(e => s"${e._1.getApi_code}，${e._1.getName}").asJava)

  }

}
