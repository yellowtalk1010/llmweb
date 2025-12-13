package zuk.sast.controller

import org.apache.commons.csv.CSVFormat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.stockapi.{LoaderLocalStockData, StockApiVo}
import zuk.tu_share.dto.HmDetail

import java.util
import java.util.stream.Collectors
import java.util.{Arrays, HashMap, List, Map, Set, UUID}
import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.jdk.CollectionConverters.*

object AllStockController{
  val hmDetailMap = scala.collection.mutable.HashMap[String, List[HmDetail]]()
}

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
    val hmFile = new File(s"tushare/hm/hm_detail/hm_detail-${tradedate}.csv") //龙虎榜路径
    println(s"游资交易每日明细:hmPath=${hmFile.exists()}, tradedate=${tradedate}, search=${search}")
    if(hmFile.exists() && AllStockController.hmDetailMap.get(tradedate).isEmpty){
      //路径存在
      val in = new FileReader(hmFile.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      val codes = records.asScala.map(record => {
          val hmDetail = new HmDetail()
          hmDetail.trade_date = record.get("trade_date")
          hmDetail.ts_code = record.get("ts_code")
          hmDetail.ts_name = record.get("ts_name")
          hmDetail.buy_amount = record.get("buy_amount")
          hmDetail.sell_amount = record.get("sell_amount")
          hmDetail.net_amount = record.get("net_amount")
          hmDetail.hm_name = record.get("hm_name")
          hmDetail.hm_orgs = record.get("hm_orgs")
          //
          hmDetail.splitTsCode(hmDetail.ts_code)
          hmDetail
        })
        .toList
      in.close()
      val countMap = codes.groupBy(_.ts_code).map(e=>(e._1, e._2.size))
      codes.foreach(c=>{
        c.count = countMap.get(c.ts_code).get //计算买入的游资数量
      })
      AllStockController.hmDetailMap.put(tradedate, codes.sortBy(_.count).reverse.asJava)
    }
    val list = new util.ArrayList[HmDetail]()
    if(AllStockController.hmDetailMap.get(tradedate).nonEmpty){
      list.addAll(AllStockController.hmDetailMap.get(tradedate).get.asScala.filter(e=>{
          scala.collection.mutable.ListBuffer(e.ts_code, e.ts_name, e.hm_name, e.hm_orgs).filter(_.contains(search)).size>0
      }).asJava)
    }
    val map: Map[String, AnyRef] = new HashMap[String, AnyRef]
    map.put("stocks", list)
    map
  }

}
