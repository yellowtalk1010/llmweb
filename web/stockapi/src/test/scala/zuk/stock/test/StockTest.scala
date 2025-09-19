package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.model.{AVG_Model, MA1_Model, MA_Model}
import zuk.stockapi.{CalculateMA, LoaderLocalStockData, StockApiVo}

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

class StockTest extends AnyFunSuite {

  def loadData(): Unit = {
    LoaderLocalStockData.loadToken()
  }

  test(""){
    this.loadData()
  }

  test("ma") {
    this.loadData()
    val codes = LoaderLocalStockData.STOCKS.asScala.filter(e=>{
      e.getApi_code.equals("600641")
        || true
    }).toList

    val tpList = CalculateMA.run(codes)

    val maModelList = ListBuffer[StockApiVo]()
    val ma1ModelList = ListBuffer[StockApiVo]()
    val avgModelList = ListBuffer[StockApiVo]()

    tpList.filter(_._2.size>0).foreach(tp=>{
      val stock = tp._1
      val malist = tp._2

      //MA递增策略
      val maModel = new MA_Model(stock, malist)
      maModel.run()
      if (maModel.isHit()) {
        maModelList += stock
      }

      //MA穿透策略
      val ma1Model = new MA1_Model(stock, malist)
      ma1Model.run()
      if (ma1Model.isHit()) {
        ma1ModelList += stock
      }

      //AVG递增策略
      val avgModel = new AVG_Model(stock, malist)
      avgModel.run()
      if (avgModel.isHit()) {
        avgModelList += stock
      }

    })

    println(s"ma模型策略: ${maModelList.size}")

    maModelList.foreach(e=>{
      println(s"${e.getApi_code}")
    })

    println("ma1模型策略")

    ma1ModelList.foreach(e => {
      println(s"${e.getApi_code}")
    })

    println("avg模型策略")
    avgModelList.foreach(e=>{
      println(s"${e.getApi_code}")
    })

    println("模型策略交集")
    (maModelList ++ ma1ModelList ++ avgModelList)
      .groupBy(_.getApi_code)
      .filter(_._2.size>1)
      .map(_._1)
      .foreach(println)


  }

}
