package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.LoaderStockData

class StockTest extends AnyFunSuite {

  def loadData(): Unit = {
    LoaderStockData.loadToken()
  }

  test(""){
    this.loadData()
  }

}
