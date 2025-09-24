package zuk.sast.controller

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.stockapi.{CalculateMAForDay, LoaderLocalStockData}

import java.text.SimpleDateFormat
import java.util
import java.util.Date
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(value=Array("stockDay"))
class StockDayController {

  private val log = LoggerFactory.getLogger(classOf[StockDayController])

  /***
   * http://localhost:8080/stockDay/list?context=000001&tradeDate=2025-09-23
   * @param context
   * @param tradeDate
   * @return
   */
  @GetMapping(value=Array("list"))
  def all(context: String, tradeDate: String): util.Map[String, Object] = {

    log.info(s"context: ${context}, tradeTime: ${tradeDate}")

    val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    var searchDatetime: String = simpleDateFormat.format(new Date())
    if(StringUtils.isNotEmpty(tradeDate)){
      searchDatetime = tradeDate
    }
    val filterList = LoaderLocalStockData.STOCKS.asScala.filter(stock=>{
      if(StringUtils.isNotEmpty(context)) {
        val st = stock.getName.contains(context) || stock.getApi_code.contains(context)
        st
      }
      else {
        true
      }
    }).toList

    //最多返回10条数据
    val searchStockList = if(filterList.size > 10) filterList.take(10) else filterList

    val stockDayVoList = searchStockList.flatMap(stock=>{
      val dayList = CalculateMAForDay.getStockDayVos(stock)
        .filter(e=>{
          e.getTime.equals(searchDatetime)
        })
      dayList.map(e=>(stock, e))
    }).map(e=>{
      val stock = e._1
      val dayInfo = e._2
      val map = new util.HashMap[String, String]()
      //val splits = stock.getApi_code.split(".")
      map.put("api_code", stock.getApi_code)
      map.put("jys", stock.getJys)
      map.put("name", stock.getName)
      map.put("time", dayInfo.getTime)
      map.put("open", dayInfo.getOpen)
      map.put("turnoverRatio", dayInfo.getTurnoverRatio)
      map.put("amount", dayInfo.getAmount)
      map.put("high", dayInfo.getHigh)
      map.put("low", dayInfo.getLow)
      map.put("changeRatio", dayInfo.getChangeRatio)
      map.put("close", dayInfo.getClose)
      map.put("volume", dayInfo.getCode)
      map
    })

    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", stockDayVoList.asJava)
    map
  }

}
