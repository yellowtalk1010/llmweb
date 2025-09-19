package zuk.stock.test

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.model.{AVG_Model, MA1_Model, MA_Model}
import zuk.stockapi.{CalculateMA, LoaderLocalStockData, StockApiVo}

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

class StockModelTest extends AnyFunSuite {

  test("模型策略") {
    LoaderLocalStockData.loadToken()
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

    val lines = ListBuffer[String]()
    //
    println(s"ma模型策略: ${maModelList.size}")
    lines += s"ma模型策略: ${maModelList.size}"
    maModelList.foreach(e=>{
      lines += e.getApi_code
      println(s"${e.getApi_code}")
    })

    //
    println("ma1模型策略")
    lines += "ma1模型策略"
    ma1ModelList.foreach(e => {
      lines += e.getApi_code
      println(s"${e.getApi_code}")
    })

    //
    println("avg模型策略")
    lines += "avg模型策略"
    avgModelList.foreach(e=>{
      lines += e.getApi_code
      println(s"${e.getApi_code}")
    })

    //
    println("模型策略交集")
    lines += "模型策略交集"
    (maModelList ++ ma1ModelList ++ avgModelList)
      .groupBy(_.getApi_code)
      .filter(_._2.size>1)
      .map(_._1)
      .foreach(code=>{
        lines += code
        println(code)
      })


    FileUtils.writeLines(new File(s"stockapi/model_result/策略结果-${new SimpleDateFormat("yyyyMMdd").format(new Date())}.txt"), "UTF-8", lines.asJava)


  }

}
