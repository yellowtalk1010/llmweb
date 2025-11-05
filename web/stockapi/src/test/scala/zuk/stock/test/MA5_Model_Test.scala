package zuk.stock.test

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.{CalculateMAForDay, LoaderLocalStockData, StockApiVo, StockMaVo}

import java.io.File
import java.math
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import scala.jdk.CollectionConverters.*

class MA5_Model_Test extends AnyFunSuite {

  test("m5") {

    LoaderLocalStockData.loadToken()
    val codes = LoaderLocalStockData.STOCKS.asScala.filter(e => {
      e.getApi_code.equals("688379")
        || true
    }).toList

    val tpList = CalculateMAForDay.run(codes)


    val socketList = tpList.filter(_._2.size > 0).filter(tp => {
      val stock = tp._1
      val malist = tp._2
      import zuk.stockapi.model.MA5_Model
      val ma55 = new MA5_Model(stock, malist)
      ma55.run()
      ma55.isHit()
//      val st = run(stock, malist)
//      st
    })

    println(socketList.size)
    socketList.map(e=>s"${e._1.getApi_code}，${e._1.getName}").foreach(println)
    val sdm = new SimpleDateFormat("yyyyMMdd")
    FileUtils.writeLines(new File(s"stockapi/model_result/MA5-${sdm.format(new Date)}.txt"), socketList.map(e=>s"${e._1.getApi_code}，${e._1.getName}").asJava)

  }



  test("主题"){
    LoaderLocalStockData.loadToken()
    val sdm = new SimpleDateFormat("yyyyMMdd")
    val files = List(s"stockapi/model_result/MA3-${sdm.format(new Date)}.txt",
      s"stockapi/model_result/MA4-${sdm.format(new Date)}.txt",
      s"stockapi/model_result/MA5-${sdm.format(new Date)}.txt")
    files.map(f=>new File(f)).filter(f=>f.exists()).flatMap(file=>{
      val lines = FileUtils.readLines(file, "UTF-8")
      lines.asScala.map(l=>{
        l.split("，")(0)
      })
    }).map(code=>LoaderLocalStockData.STOCKS.asScala.filter(_.getApi_code.equals(code)).head)
      .flatMap(stock=>stock.getGl.split(","))
      .groupBy(e=>e)
      .toList
      .sortBy(_._2.size)
      .reverse
      .foreach(tp=>{
        println(s"${tp._1}，${tp._2.size}")
      })

  }

}
