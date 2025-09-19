package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.{CalculateMA, LoaderStockData}

import scala.jdk.CollectionConverters.*

class StockTest extends AnyFunSuite {

  def loadData(): Unit = {
    LoaderStockData.loadToken()
  }

  test(""){
    this.loadData()
  }

  test("ma") {
    this.loadData()
    val codes = LoaderStockData.STOCKS.asScala.filter(e=>{
      e.getApi_code.equals("603406")
//        || true
    }).toList
    CalculateMA.run(codes)
  }

}
