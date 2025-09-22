package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.{DownloadMinuteStock, LoaderLocalStockData}

import java.util.concurrent.{Executors, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*

class DownloadStockMinuteTest extends AnyFunSuite {

  test("并发执行"){
    LoaderLocalStockData.loadToken()
    val executor = Executors.newFixedThreadPool(10)
    val counter = new AtomicInteger(0)
    LoaderLocalStockData.STOCKS.asScala.foreach(stock=>{
      executor.execute(()=>{
        DownloadMinuteStock.run(List(stock))
        counter.incrementAndGet()
        println(s"${counter.get()}/${LoaderLocalStockData.STOCKS.size()}")
      })
    })
    executor.shutdown
    if (!executor.awaitTermination(5, TimeUnit.MINUTES))
      println("线程池在指定时间内未完成")
    else
      println("所有任务已完成")

  }

}
