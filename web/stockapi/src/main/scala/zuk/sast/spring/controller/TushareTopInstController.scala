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

@RestController
@RequestMapping(value=Array("top_inst"))
class TushareTopInstController {

  private val log = LoggerFactory.getLogger(classOf[TushareTopInstController])

  @Autowired
  private var applicationProperties: ApplicationProperties = null

  @PostConstruct
  def init(): Unit = {
    log.info("加载龙虎榜数据")
    val stockHmTopInstPath = applicationProperties.getStockDatasourceBuildSystem_stockHmTopInstPath
    TopInstUtil.loadData(stockHmTopInstPath)
  }

  /**
   * 龙虎榜机构交易单
   * top_inst
   */
  @GetMapping(value=Array("list"))
  def all(search: String, tradedate: String): util.Map[String, Object] = {
    log.info(s"龙虎榜查询接口入参：search: ${search}, tradedate: ${tradedate}")
    val date = if(StringUtils.isNotBlank(tradedate)){
      tradedate.replaceAll("-", "")
    }
    else {
      tradedate
    }
    log.info(s"龙虎榜查询接口入参：search: ${search}, date: ${date}")

    if(StringUtils.isEmpty(search)
      && StringUtils.isEmpty(date)){
      return new util.HashMap[String, Object]()
    }

    val stockHmTopInstPath = applicationProperties.getStockDatasourceBuildSystem_stockHmTopInstPath
    TopInstUtil.loadData(stockHmTopInstPath)

    val list = new util.ArrayList[TopInst]()
    if(StringUtils.isNotBlank(date)){
      //根据日期选择
      val ls = TopInstUtil.loadData(stockHmTopInstPath).get(date).get.asScala.filter(e=>{
        scala.collection.mutable.ListBuffer(e.ts_code,e.ts_name,e.hm_name,e.exalter).filter(s=>s!=null && s.contains(search)).size>0
      })
      list.addAll(ls.asJava)
    }
    else {
      //根据日期排序
      val ls = TopInstUtil.loadData(stockHmTopInstPath).toList.sortBy(e=>e._1).reverse.flatMap(_._2.asScala).filter(e=>{
        scala.collection.mutable.ListBuffer(e.ts_code,e.ts_name,e.hm_name,e.exalter).filter(s=>s!=null && s.contains(search)).size>0
      })
      list.addAll(ls.asJava)
    }

    //
    val buySum = list.asScala.filter(e=>StringUtils.isNotBlank(e.buy)).map(e=>{
      try {
        e.buy.toDouble
      }
      catch {
        case exception: Exception =>
          exception.printStackTrace()
          0.0
      }
    }).sum
    val selSum = list.asScala.filter(e=>StringUtils.isNotBlank(e.sell)).map(e=>{
      try {
        e.sell.toDouble
      }
      catch {
        case exception: Exception =>
          exception.printStackTrace()
          0.0
      }
    }).sum
    val netSum = list.asScala.filter(e=>StringUtils.isNotBlank(e.net_buy)).map(e=>{
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
    list.addFirst(sumTopInst)

    list.asScala.map(e=>{
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
    map.put("data", list)
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

