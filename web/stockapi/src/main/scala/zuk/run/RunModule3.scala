package zuk.run

import org.apache.commons.csv.CSVFormat
import zuk.stockapi.{CalculateMAForDay_Tushare, StockApiVo}
import zuk.stockapi.model.{MA3_1_Model, MA3_Model}
import zuk.utils.SendMail

import java.io.{File, FileReader}
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import scala.jdk.CollectionConverters.*

object RunModule3 {

  var PATH = ""
  val mailAddress = "513283439@qq.com"

  def handle(path: String): Unit = {
    val dir = new File(path)
    if(!dir.exists() || !dir.isDirectory){
      println(s"${dir.getAbsolutePath}不存在")
      System.exit(1)
    }

    val all_stocks_file = new File(path + File.separator + "all_stocks.csv")
    if (!all_stocks_file.exists() || !all_stocks_file.isFile){
      println(s"${all_stocks_file.getAbsolutePath}不存在")
      System.exit(1)
    }

    PATH = path
    val sdf = new SimpleDateFormat("yyyyMMdd")
    val tradeDate = sdf.format(new Date())

    //将tushare的csv数据转成对象
    val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record=>{
        val ts_code = record.get("ts_code")
        val name = record.get("name")
        val splits = ts_code.split("\\.")
        val code = splits(0)
        val jys = splits(1)
        val gl = record.get("industry")
        val area = record.get("area")
        val stockApiVo = new StockApiVo()
        stockApiVo.setApi_code(code)
        stockApiVo.setName(name)
        stockApiVo.setJys(jys)
        stockApiVo.setGl(gl)
        stockApiVo.setArea(area)
        stockApiVo
      })
      //      .filter(e=>e.getApi_code.contains("000753"))
      .toList
    in.close()
    println(s"${codes.size}")

    val tpList = CalculateMAForDay_Tushare.run(codes)

    println("模型3号")
    val socketListM3 = tpList.filter(_._2.size > 0).filter(tp => {
        val stock = tp._1
        val malist = tp._2
        val model = new MA3_Model(stock, malist)
        model.run()
        model.isHit()
      })
      .filter(e=> !e._1.getName.contains("ST")) //排除ST股票

    println(socketListM3.size)
    socketListM3.map(e => e._1.getApi_code + "，" + e._1.getName).foreach(println)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}-推荐 M3", socketListM3.map(e=>s"${e._1.getApi_code}，${e._1.getName}，${e._1.getArea}，${e._1.getGl}").mkString("\n"))


    println("模型3.1号")
    val socketListM3_1 = tpList.filter(_._2.size > 0).filter(tp => {
        val stock = tp._1
        val malist = tp._2
        val model = new MA3_1_Model(stock, malist)
        model.run()
        model.isHit()
      })
      .filter(e => !e._1.getName.contains("ST")) //排除ST股票

    println(socketListM3_1.size)
    socketListM3_1.map(e => e._1.getApi_code + "，" + e._1.getName).foreach(println)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"${tradeDate}-推荐 M3.1", socketListM3_1.map(e => s"${e._1.getApi_code}，${e._1.getName}，${e._1.getArea}，${e._1.getGl}").mkString("\n"))

  }

}
