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

class MA6_Model_Test extends AnyFunSuite {

  test("m6") {

    LoaderLocalStockData.loadToken()
    val codes = LoaderLocalStockData.STOCKS.asScala.filter(e => {
      e.getApi_code.equals("688379")
        || true
    }).toList

    val tpList = CalculateMAForDay.run(codes)


    val socketList = tpList.filter(_._2.size > 0).filter(tp => {
      val stock = tp._1
      val malist = tp._2
      import zuk.stockapi.model.MA6_Model
      val ma = new MA6_Model(stock, malist)
      ma.run()
      ma.isHit()
//      val st = run(stock, malist)
//      st
    })

    println(socketList.size)
    socketList.map(e=>s"${e._1.getApi_code}，${e._1.getName}").foreach(println)
    val sdm = new SimpleDateFormat("yyyyMMdd")
    FileUtils.writeLines(new File(s"stockapi/model_result/${sdm.format(new Date)}-MA6.txt"), socketList.map(e=>s"${e._1.getApi_code}，${e._1.getName}").asJava)

  }


}
