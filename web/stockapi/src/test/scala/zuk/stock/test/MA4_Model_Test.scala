package zuk.stock.test

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.model.MA4_Model
import zuk.stockapi.{CalculateMAForDay, LoaderLocalStockData}

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import scala.jdk.CollectionConverters.*

class MA4_Model_Test extends AnyFunSuite {

  test("M4") {

    LoaderLocalStockData.loadToken()
    val codes = LoaderLocalStockData.STOCKS.asScala.filter(e => {
      e.getApi_code.equals("002490")
        || true
    }).toList

    val tpList = CalculateMAForDay.run(codes)

    val socketList = tpList.filter(_._2.size > 0).filter(tp => {
      val stock = tp._1
      val malist = tp._2
      val model = new MA4_Model(stock, malist)
      model.run()
      model.isHit()
    })

    println(socketList.size)
    socketList.map(e=>e._1.getApi_code + "，" + e._1.getName).foreach(println)
    val sdm = new SimpleDateFormat("yyyyMMdd")
    FileUtils.writeLines(new File(s"stockapi/model_result/MA4-${sdm.format(new Date)}.txt"), socketList.map(e=>s"${e._1.getApi_code}，${e._1.getName}").asJava)

  }

}
