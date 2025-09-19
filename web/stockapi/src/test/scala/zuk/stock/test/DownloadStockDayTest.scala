package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.{DownloadDayStock, LoaderLocalStockData}


class DownloadStockDayTest extends AnyFunSuite {

  test("个股信息更新"){
    LoaderLocalStockData.loadToken()
    DownloadDayStock.run()
  }



}
