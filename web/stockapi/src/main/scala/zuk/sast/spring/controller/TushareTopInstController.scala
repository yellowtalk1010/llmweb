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

    if(StringUtils.isEmpty(search)
      && StringUtils.isEmpty(tradedate)){
      return new util.HashMap[String, Object]()
    }

    val stockHmTopInstPath = applicationProperties.getStockDatasourceBuildSystem_stockHmTopInstPath
    TopInstUtil.loadData(stockHmTopInstPath)

    val list = new util.ArrayList[TopInst]()
    if(StringUtils.isNotBlank(tradedate)){
      //根据日期选择
      val ls = TopInstUtil.loadData(stockHmTopInstPath).get(tradedate).get.asScala.filter(e=>{
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
    val buySum = list.asScala.map(e=>{
      if(e.buy==null){
        0.0d
      }
      else {
        e.buy.toDouble
      }
    }).sum
    val selSum = list.asScala.map(e=>{
      if(e.sell==null){
        0.0d
      }
      else {
        e.sell.toDouble
      }
    }).sum
    val netSum = list.asScala.map(e=>{
      if(e.net_buy==null){
        0.0d
      }
      else {
        e.net_buy.toDouble
      }
    }).sum
    val sumTopInst = new TopInst
    sumTopInst.buy = "+" + buySum.toString
    sumTopInst.sell = "-" + selSum.toString
    sumTopInst.net_buy = netSum.toString
    list.add(sumTopInst)


    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", list)
    map
  }

}

