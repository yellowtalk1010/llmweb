package zuk.sast.spring.controller

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.tu_share.dto.TopInst
import zuk.tu_share.utils.TopInstUtil

import java.util
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*
import org.springframework.beans.factory.annotation.{Autowired, Value}
import zuk.sast.spring.controller.component.ApplicationProperties

import java.io.File
import java.text.SimpleDateFormat
import scala.collection.mutable.ListBuffer

@RestController
@RequestMapping(value=Array("top_inst"))
class TushareTopInstController {

  private val log = LoggerFactory.getLogger(classOf[TushareTopInstController])

  @Autowired
  private var applicationProperties: ApplicationProperties = null

  @PostConstruct
  def init(): Unit = {
    log.info("加载龙虎榜数据")
    val stockHmTopInstPath = applicationProperties.getStockDatasourceBuildSystem_stockHmTopInstPath + File.separator + "2026"
    TopInstUtil.loadData(stockHmTopInstPath)
  }

  /**
   * 龙虎榜机构交易单
   * top_inst
   */
  @GetMapping(value=Array("list"))
  def all(search: String, tradedate: String, group: Boolean): util.Map[String, Object] = {

    log.info(s"龙虎榜查询接口入参：search: ${search}, tradedate: ${tradedate}, group: ${group}")
    val date = if(StringUtils.isNotBlank(tradedate)){
      tradedate.replaceAll("-", "")
    }
    else {
      tradedate
    }
    log.info(s"龙虎榜查询接口入参：search: ${search}, date: ${date}, group: ${group}")

    //数据
    var dataList = new util.ArrayList[TopInst]()
    //日期
    val dateList = new util.ArrayList[String]()

    val ls = if(StringUtils.isNotBlank(search)){
      //如果输入了查询数据
      TopInstUtil.topInstMap.toList.sortBy(e => e._1).reverse.flatMap(_._2.asScala).filter(e => {
        scala.collection.mutable.ListBuffer(e.ts_code, e.ts_name, e.hm_name, e.exalter).filter(s => s != null && s.contains(search)).size > 0
      })
    }
    else {
      //如果没有输入查询数据
      TopInstUtil.topInstMap.toList.sortBy(e=>e._1).reverse.flatMap(_._2.asScala)
    }

    log.info(s"根据条件获取龙虎榜总数据:${ls.size}")
    dateList.addAll(ls.map(_.trade_date).toSet.toList.asJava)

    if(StringUtils.isBlank(search) && StringUtils.isBlank(date)){
      //数据返回空
    }
    else {
      dataList.addAll(ls.filter(e => {
        if (StringUtils.isNotBlank(date)) {
          e.trade_date.equals(date)
        }
        else {
          true
        }
      }).asJava)

    }
    if(group){
      //如何选择聚合
      log.info("龙虎榜数据进行聚合")
      val ls1 = dataList.asScala.groupBy(e=>e.ts_code)
        .filter(tp2=>tp2._2.filter(e=>StringUtils.isNotEmpty(e.buy) && StringUtils.isNotEmpty(e.sell) && StringUtils.isNotEmpty(e.net_buy)).toList.size>0)
        .map(tp2=>{
          println(s"${tp2._1}, ${tp2._2.toList.size}")
          val ls = tp2._2.toList
          val topInst = new TopInst
          topInst.ts_code = ls.head.ts_code
          topInst.ts_name = ls.head.ts_name
          topInst.buy = ls.map(_.buy.toFloat).sum.toString
          topInst.sell = ls.map(_.sell.toFloat).sum.toString
          topInst.net_buy = ls.map(_.net_buy.toFloat).sum.toString
          topInst.easyMoneyURL = ls.head.easyMoneyURL
          topInst
        }).toList.asJava

      dataList.clear()
      dataList.addAll(ls1)
    }

    log.info(s"过滤日期龙虎榜总数据:${dataList.size}")

    log.info(s"龙虎榜聚合输出:\n${dataList.asScala.map(e => s"${e.ts_code}, ${e.ts_name}, ${e.easyMoneyURL}").toSet.toList.sorted.mkString("\n")}")



    if(dataList.size()>0){

      //
      val buySum = dataList.asScala.filter(e => StringUtils.isNotBlank(e.buy)).map(e => {
        try {
          e.buy.toDouble
        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
            0.0
        }
      }).sum
      val selSum = dataList.asScala.filter(e => StringUtils.isNotBlank(e.sell)).map(e => {
        try {
          e.sell.toDouble
        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
            0.0
        }
      }).sum
      val netSum = dataList.asScala.filter(e => StringUtils.isNotBlank(e.net_buy)).map(e => {
        try {
          e.net_buy.toDouble
        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
            0.0
        }
      }).sum

      val sumTopInst = new TopInst
      sumTopInst.ts_name = "合并结果"
      sumTopInst.buy = "+" + buySum.toString
      sumTopInst.sell = "-" + selSum.toString
      sumTopInst.net_buy = netSum.toString
      dataList.addFirst(sumTopInst)
    }


    dataList.asScala.map(e=>{
      if(StringUtils.isNotBlank(e.buy)){
        e.buyDesc = formatChineseUnit(e.buy.toDouble)
      }
      if (StringUtils.isNotBlank(e.sell)) {
        e.sellDesc = formatChineseUnit(e.sell.toDouble)
      }
      if (StringUtils.isNotBlank(e.net_buy)) {
        e.netBuyDesc = formatChineseUnit(e.net_buy.toDouble)
      }
    })

    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", dataList)
    map.put("date", dateList.asScala.filter(e=>StringUtils.isNotBlank(e)).toSet.toList.sorted.reverse.map(e=>{
      s"${e.substring(0,4)}-${e.substring(4,6)}-${e.substring(6,8)}"
    }).asJava)
    map
  }

  def formatChineseUnit(value: Double): String = {
    import java.math.BigDecimal
    import java.math.RoundingMode
    val negative = value < 0

    val num = BigDecimal.valueOf(Math.abs(value))

    var result: String = null

    if (num.compareTo(new BigDecimal("100000000")) >= 0) {
      result = num.divide(new BigDecimal("100000000"), 2, RoundingMode.HALF_UP).stripTrailingZeros.toPlainString + "亿"
    } else if (num.compareTo(new BigDecimal("10000000")) >= 0) {
      result = num.divide(new BigDecimal("10000000"), 2, RoundingMode.HALF_UP).stripTrailingZeros.toPlainString + "千万"
    } else if (num.compareTo(new BigDecimal("10000")) >= 0) {
      result = num.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP).stripTrailingZeros.toPlainString + "万"
    } else {
      result = num.stripTrailingZeros.toPlainString
    }

    val res = if (negative) {
      "-" + result
    } else {
      result
    }

    res

  }

}

