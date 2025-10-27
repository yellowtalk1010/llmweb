//package zuk.stock.test
//
//import org.apache.commons.lang3.StringUtils
//import org.scalatest.funsuite.AnyFunSuite
//import zuk.stockapi.{DownloadMinuteStock, LoaderLocalStockData, StockApiVo}
//
//import java.util.concurrent.{Executors, TimeUnit}
//import java.util.concurrent.atomic.AtomicInteger
//import scala.jdk.CollectionConverters.*
//
//class DownloadStockMinuteTest extends AnyFunSuite {
//
////  test("昨日重点关注") {
////    LoaderLocalStockData.loadToken()
////    val stocks = getAllData()
////    DownloadMinuteStock.run(stocks)
////  }
//
//  test("下载1"){
//    LoaderLocalStockData.loadToken()
//    val stocks = LoaderLocalStockData.STOCKS.asScala.sortBy(_.getApi_code).toList
//    DownloadMinuteStock.run(stocks)
//  }
//
//  test("下载2") {
//    LoaderLocalStockData.loadToken()
//    val stocks = LoaderLocalStockData.STOCKS.asScala.sortBy(_.getApi_code).reverse.toList
//    DownloadMinuteStock.run(stocks)
//  }
////
////  test("下载3") {
////    LoaderLocalStockData.loadToken()
////    //    val stocks = getAllData()
////    val stocks = LoaderLocalStockData.STOCKS.asScala.slice(2000,3000).toList
////    DownloadMinuteStock.run(stocks)
////  }
////
////  test("下载4") {
////    LoaderLocalStockData.loadToken()
////    //    val stocks = getAllData()
////    val stocks = LoaderLocalStockData.STOCKS.asScala.slice(4000,5000).toList
////    DownloadMinuteStock.run(stocks)
////  }
////
////  test("下载5") {
////    LoaderLocalStockData.loadToken()
////    val stocks = LoaderLocalStockData.STOCKS.asScala.slice(5000,LoaderLocalStockData.STOCKS.size()-1).toList
////    DownloadMinuteStock.run(stocks)
////  }
//
//
//  def getAllData(): List[StockApiVo] = {
//
//    val list = """
//                 |ma2模型策略
//                 |600105
//                 |603322
//                 |300001
//                 |603269
//                 |600375
//                 |600619
//                 |002065
//                 |600857
//                 |600577
//                 |300260
//                 |300088
//                 |300913
//                 |300846
//                 |603650
//                 |301133
//                 |301326
//                 |301319
//                 |001311
//                 |301291
//                 |688729
//                 |ma1模型策略
//                 |300250
//                 |603757
//                 |603322
//                 |600895
//                 |603269
//                 |600375
//                 |600857
//                 |002169
//                 |300655
//                 |300260
//                 |688550
//                 |605358
//                 |300088
//                 |300354
//                 |300438
//                 |300450
//                 |603178
//                 |301133
//                 |688332
//                 |301319
//                 |001311
//                 |301291
//      |
//      |""".stripMargin
//
//
//    val sets = list.split("\r\n").filter(e=>StringUtils.isNotEmpty(e) && e.size==6).toSet
//    LoaderLocalStockData.STOCKS.asScala.filter(e=>sets.contains(e.getApi_code)).toList
//  }
//
//}
