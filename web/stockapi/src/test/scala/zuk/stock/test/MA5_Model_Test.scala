package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.{CalculateMAForDay, LoaderLocalStockData}

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
      val model = new zuk.stockapi.model.MA55_Model(stockMaVo = stock, maList = malist)
//      val model = new MA5_Model(stock, malist)
      model.run()
      model.isHit()
    })

    println(socketList.size)
    socketList.map(_._1.getApi_code).foreach(println)

  }

}
