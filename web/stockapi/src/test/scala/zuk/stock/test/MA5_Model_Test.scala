package zuk.stock.test

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.{CalculateMAForDay, LoaderLocalStockData, StockApiVo, StockMaVo}

import java.io.File
import java.math
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
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
      import zuk.stockapi.model.MA55_Model
      val ma55 = new MA55_Model(stock, malist)
      ma55.run()
      ma55.isHit()
//      val st = run(stock, malist)
//      st
    })

    println(socketList.size)
    socketList.map(e=>s"${e._1.getApi_code}，${e._1.getName}").foreach(println)
    val sdm = new SimpleDateFormat("yyyyMMdd")
    FileUtils.writeLines(new File(s"stockapi/model_result/MA5-${sdm.format(new Date)}.txt"), socketList.map(_._1.getApi_code).asJava)

  }



//  def run(stockMaVo: StockApiVo, maList: List[StockMaVo]): Boolean = {
//    if(maList.size>=3){
//      val list = maList.take(3)
//      val day0 = list(0)
//      val day1 = list(1)
//      val day2 = list(2)
//      if(new BigDecimal(day0.getMa5).compareTo(new BigDecimal(day1.getMa5)) > 0
//        && new BigDecimal(day1.getMa5).compareTo(new BigDecimal(day2.getMa5)) > 0 //递增
//
//        && new BigDecimal(day0.getMa10).compareTo(new BigDecimal(day1.getMa10)) < 0
//        && new BigDecimal(day1.getMa10).compareTo(new BigDecimal(day2.getMa10)) < 0 //递减
//
//        && new BigDecimal(day0.getMa10).compareTo(new BigDecimal(day0.getMa5)) > 0
//        && new BigDecimal(day1.getMa10).compareTo(new BigDecimal(day1.getMa5)) > 0
////        && new BigDecimal(day2.getMa10).compareTo(new BigDecimal(day2.getMa5)) > 0 //
//
//        && new BigDecimal(day0.getStockDayVo.getTurnoverRatio).compareTo(new BigDecimal(4)) >=0
//        && new BigDecimal(day1.getStockDayVo.getTurnoverRatio).compareTo(new BigDecimal(4)) >=0 //huan shou
////
//        && new BigDecimal(day0.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(1)) >=0
//        && new BigDecimal(day1.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(1)) >=0   //zhang fu
//      ){
//
//        if(yuce(maList)){
//          return true
//        }
//
//      }
//    }
//
//    false
//  }
//
//
//  /***
//   * shang zhang 3 上升
//   * @param maList
//   * @return
//   */
//  private def yuce(maList: List[StockMaVo]): Boolean = {
//    val lastDay = maList.head
//    val close = lastDay.getStockDayVo.getClose
//    val ycj = (new BigDecimal(close).multiply(new BigDecimal("0.03"))).add(new BigDecimal(close))
//
//    val m5list = List(ycj) ++ maList.take(4).map(e=>{
//      new BigDecimal(e.getStockDayVo.getClose)
//    })
//    val new5 = m5list.reduceOption[BigDecimal]((a,b)=>{
//      a.add(b)
//    }).get.divide(new BigDecimal(5), 5, math.BigDecimal.ROUND_HALF_DOWN)
//
//    val m10list = List(ycj) ++ maList.take(9).map(e => {
//      new BigDecimal(e.getStockDayVo.getClose)
//    })
//
//    val new10 = m10list.reduceOption[BigDecimal]((a,b)=>{
//      a.add(b)
//    }).get.divide(new BigDecimal(10), 5, math.BigDecimal.ROUND_HALF_DOWN)
//
//    val st = new5.compareTo(new10) > 0
//
//    st
//  }

}
