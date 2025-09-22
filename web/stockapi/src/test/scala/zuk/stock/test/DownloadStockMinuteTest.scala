package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.{DownloadMinuteStock, LoaderLocalStockData}

import scala.jdk.CollectionConverters.*

class DownloadStockMinuteTest extends AnyFunSuite {

  test(""){
    LoaderLocalStockData.loadToken()
    DownloadMinuteStock.run(
      LoaderLocalStockData.STOCKS.asScala.filter(e=>{
        List("000001").contains(e.getApi_code)
      }).toList
    )
  }

}
