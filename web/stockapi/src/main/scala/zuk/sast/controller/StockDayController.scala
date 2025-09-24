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
   * http://localhost:8080/stockDay/list?search=000001&tradeTime=2025-09-23
   * @param search
   * @param tradeTime
   * @return
   */
  @GetMapping(value=Array("list"))
  def all(search: String, tradeTime: String): util.Map[String, Object] = {

    log.info(s"search: ${search}, tradeTime: ${tradeTime}")

    val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    var searchDatetime: String = simpleDateFormat.format(new Date())
    if(StringUtils.isNotEmpty(tradeTime)){
      searchDatetime = tradeTime
    }
    val filterList = LoaderLocalStockData.STOCKS.asScala.filter(stock=>{
      if(StringUtils.isNotEmpty(search)) {
        val st = stock.getName.contains(search) || stock.getApi_code.contains(search)
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
      dayList
    })

    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", stockDayVoList.asJava)
    map
  }

}
