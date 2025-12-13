package zuk.sast.controller

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.stockapi.{LoaderLocalStockData, StockApiVo}
import zuk.tu_share.dto.HmDetail
import zuk.tu_share.utils.HmDetailUtil

import java.util
import java.util.stream.Collectors
import java.util.{HashMap, Map, Set, UUID}
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(value = Array("stock"))
class AllStockController {

  private val log = LoggerFactory.getLogger(classOf[AllStockController])

  @Autowired
  private var stockMapper: StockMapper = _

  @GetMapping(value = Array("delete"))
  def delete(api_code: String): util.Map[String, Object] = {
    log.info("delete:" + api_code)
    val result: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
    try {
      this.stockMapper.deleteByCode(api_code)
      result.put("status", "ok")
      return result
    } catch {
      case e: Exception =>
        //e.printStackTrace();
        log.error(e.getMessage)
        result.put("status", e.getMessage)
    }
    result
  }

  @GetMapping(value = Array("add"))
  def add(api_code: String): util.Map[String, Object] = {
    log.info("add:" + api_code)
    val result = new util.HashMap[String, Object]
    try {
      val list = this.stockMapper.selectByCode(api_code)
      if (list != null && list.size > 0) {
        result.put("status", "已存在")
        return result
      }
      else {
        LoaderLocalStockData.STOCKS.stream.filter((stock: StockApiVo) => stock.getApi_code == api_code).forEach((socket: StockApiVo) => {
          val stockEntity = new StockEntity
          stockEntity.id = UUID.randomUUID.toString
          stockEntity.code = socket.getApi_code
          stockEntity.jys = socket.getJys
          stockEntity.name = socket.getName
          this.stockMapper.insert(stockEntity)
          System.out.println("插入数据库成功")
        })
        result.put("status", "ok")
        return result
      }
    } catch {
      case e: Exception =>
        //e.printStackTrace();
        log.error(e.getMessage)
        result.put("status", e.getMessage)
    }
    result
  }

  /** *
   * 我的关注
   *
   * @return
   */
  @GetMapping(value = Array("my")) def my: util.Map[String, Object] = {
    try {
      val stockEntities = stockMapper.selectAll()
      val map = new util.HashMap[String, AnyRef]
      val sets = stockEntities.stream.map((e: StockEntity) => e.code).collect(Collectors.toSet)
      val ls = LoaderLocalStockData.STOCKS.stream.filter((stock: StockApiVo) => {
        sets.contains(stock.getApi_code)

      }).toList
      map.put("stocks", ls)
      return map
    } catch {
      case e: Exception =>
        e.printStackTrace()
        log.error(e.getMessage)
    }
    new util.HashMap[String, Object]
  }

  /***
   * 游资交易每日明细
   */
  @GetMapping(value = Array("all"))
  def all(tradedate: String, search: String): Map[String, Object] = {
    println(s"tradedate:${tradedate}, search:${search}")
    HmDetailUtil.loadData()
    val list = new util.ArrayList[HmDetail]()
    if(StringUtils.isNotBlank(tradedate)){
      if(HmDetailUtil.hmDetailMap.get(tradedate).nonEmpty){
        list.addAll(HmDetailUtil.hmDetailMap.get(tradedate).get.asJava)
      }
    }
    else {
      list.addAll(HmDetailUtil.hmDetailMap.flatMap(_._2).toList.asJava)
    }

    val totalList = list.asScala.filter(e=>{
      if(StringUtils.isBlank(search)){
        true
      }
      else {

        val searchList = search.split("&").filter(e=>StringUtils.isNotBlank(e)).map(_.trim)
        val ls = searchList.filter(line=>{
            scala.collection.mutable.ListBuffer(e.ts_code, e.ts_name, e.hm_name, e.hm_orgs).filter(_.contains(line)).size > 0
        })
        ls.size == searchList.size
      }
    })

    val lastMerge = new HmDetail()
    lastMerge.sell_amount = "卖出：" + totalList.map(_.sell_amount.toFloat).sum.toString
    lastMerge.buy_amount = "买入：" + totalList.map(_.buy_amount.toFloat).sum.toString
    lastMerge.net_amount = "E8（一亿）：" + totalList.map(_.net_amount.toFloat).sum.toString

    val map: Map[String, AnyRef] = new HashMap[String, AnyRef]
    map.put("stocks", (totalList.sortWith((a,b)=>{
      if(a.trade_date.equals(b.trade_date)){
        a.count > b.count
      }
      else {
        a.trade_date.toInt > b.trade_date.toInt
      }
    } ) += lastMerge).asJava)
    map
  }

}
