package zuk.sast.controller

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.controller.StockBiddingController.JJQC_RESPONSE_MAP
import zuk.sast.controller.response.{JJQCData, JJQCResponse}
import zuk.stockapi.utils.HttpClientUtil
import zuk.stockapi.{LoaderLocalStockData, StockApiVo}

import java.text.SimpleDateFormat
import java.util
import java.util.{ArrayList, Date, LinkedHashMap, List, Map}
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import scala.jdk.CollectionConverters.*

/** *
 * 早盘、收盘竞价接口
 */
@RestController
@RequestMapping(value = Array("stockBidding"))
class StockBiddingController {

  private val log = LoggerFactory.getLogger(classOf[StockBiddingController])

  @GetMapping(value = Array("merge"))
  def merge(period: String): JJQCResponse = {
    val jjqcResponse1 = JJQC_RESPONSE_MAP.get(period + "_1")
    val jjqcResponse2 = JJQC_RESPONSE_MAP.get(period + "_2")
    val jjqcResponse3 = JJQC_RESPONSE_MAP.get(period + "_3")
    val jjqcResponse4 = JJQC_RESPONSE_MAP.get(period + "_4")
    if (jjqcResponse1 != null && jjqcResponse2 != null && jjqcResponse3 != null && jjqcResponse4 != null) {
      val list = new util.ArrayList[String]
      list.addAll(jjqcResponse1.data.stream.map((e: JJQCData) => e.code).collect(Collectors.toSet))
      list.addAll(jjqcResponse2.data.stream.map((e: JJQCData) => e.code).collect(Collectors.toSet))
      list.addAll(jjqcResponse3.data.stream.map((e: JJQCData) => e.code).collect(Collectors.toSet))
      list.addAll(jjqcResponse4.data.stream.map((e: JJQCData) => e.code).collect(Collectors.toSet))

      /***
       * Map<String, Long> sorted = list.stream()
       * .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
       * .entrySet().stream()
       * .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // 按 value 降序
       * .collect(Collectors.toMap(
       * Map.Entry::getKey,
       * Map.Entry::getValue,
       * (e1, e2) -> e1,              // 处理 key 冲突（不会发生）
       * LinkedHashMap::new           // 保持有序
       * ));
       */

      val sorted = list.asScala.groupBy(e=>e).toList.sortBy(tp2=>{
        tp2._2.size
      }).reverse.map(e=>(e._1, e._2.size)).asJava


      /***
       * for (i <- 4 until 0 by -1)  { val ll: Long = i
       System.out.println("------------------------------重复" + i + "次")
       sorted.entrySet.stream.filter((entry: util.Map.Entry[String, Long]) => {
       entry.getValue eq ll

       } ).forEach((entry: util.Map.Entry[String, Long]) => {
       System.out.println(entry.getKey)

       } )
       }
       */
      for (i <- 4 until 0 by -1) {
        val ll = i
        println("------------------------------重复" + i + "次")
        sorted.asScala.filter(e=>{
          e._2==ll
        }).foreach(tp2=>{
          println(tp2._1)
        })
      }
      val jjqcResponse = new JJQCResponse
      jjqcResponse.code = "20000"
      jjqcResponse.msg = "OKOKOK"
      jjqcResponse
    }
    else {
      log.info("合并数据不全")
      val jjqcResponse = new JJQCResponse
      jjqcResponse.code = "500"
      jjqcResponse.msg = "合并数据不全"
      jjqcResponse
    }
  }


  /** *
   * 竞价强筹
   *
   * @param period 时期，抢筹类型，0-竞价抢筹，1-尾盘抢筹
   * @param type   1委托金额排序  2成交金额排序  3开盘金额排序 4涨幅排序
   * @return
   *
   */
  @GetMapping(value = Array("jjqc"))
  def jjqc(period: String, `type`: String, tradeDate: String): JJQCResponse = {
    var tradeDateCopy = tradeDate
    if (tradeDateCopy == null || StringUtils.isEmpty(tradeDateCopy)) {
      tradeDateCopy = new SimpleDateFormat("yyyy-MM-dd").format(new Date)
    }
    val key = period + "_" + `type` + "_" + tradeDateCopy
    if (JJQC_RESPONSE_MAP.get(key) != null) return JJQC_RESPONSE_MAP.get(key)
    val url = "https://stockapi.com.cn/v1/base/jjqc?period=" + period + "&type=" + `type` + "&tradeDate=" + tradeDateCopy + "&token=" + LoaderLocalStockData.TOKEN
    println(url)
    try {
      val string = HttpClientUtil.sendGetRequest(url)
      val response = JSONObject.parseObject(string, classOf[JJQCResponse])
      response.data.stream.forEach((e: JJQCData) => {
        val stockApiVOList = LoaderLocalStockData.STOCKS.stream.filter((stock: StockApiVo) => stock.getApi_code == e.code).toList
        if (stockApiVOList != null && stockApiVOList.size > 0) {
          e.jys = stockApiVOList.get(0).getJys
          e.gl = stockApiVOList.get(0).getGl
        }

      })
      if (response.data != null && response.data.size > 0) {
        JJQC_RESPONSE_MAP.put(key, response)
      }
      response
    } catch {
      case e: Exception =>
        e.printStackTrace()
        log.error(e.getMessage)
        val response = new JJQCResponse
        response.code = "500"
        response.msg = e.getMessage
        response
    }
  }


}

object StockBiddingController {
  private val JJQC_RESPONSE_MAP = new ConcurrentHashMap[String, JJQCResponse]
}
