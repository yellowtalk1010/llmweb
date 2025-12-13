package zuk.sast.controller

import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.stockapi.{CalculateMAForDay, LoaderLocalStockData}
import zuk.tu_share.dto.{HmDetail, TopInst}

import java.io.{File, FileReader}
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util
import java.util.{Date, List}
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(value=Array("top_inst"))
class TopInstController {

  private val log = LoggerFactory.getLogger(classOf[TopInstController])

  private val topInstMap = scala.collection.mutable.HashMap[String, List[TopInst]]()

  /**
   * 龙虎榜机构交易单
   * top_inst
   */
  @GetMapping(value=Array("list"))
  def all(search: String, tradedate: String): util.Map[String, Object] = {

    log.info(s"search: ${search}, tradedate: ${tradedate}")

    val topInstFile = new File(s"tushare/hm/top_inst/${tradedate}_top_inst.csv")
    println(s"龙虎榜机构交易单:topInstFile=${topInstFile.exists()}, tradedate=${tradedate}, search=${search}")

    if (topInstFile.exists()) {
      //路径存在
      val in = new FileReader(topInstFile.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      val codes = records.asScala.map(record => {
          val topInst = new TopInst()
          topInst.trade_date = record.get("trade_date")
          topInst.ts_code = record.get("ts_code")
          topInst.exalter = record.get("exalter")
          topInst.buy = record.get("buy")
          topInst.buy_rate = record.get("buy_rate")
          topInst.sell = record.get("sell")
          topInst.sell_rate = record.get("sell_rate")
          topInst.net_buy = record.get("net_buy")
          topInst.side = record.get("side")
          topInst.reason = record.get("reason")

          //额外计算
          val ls = AllStockController.hmDetailMap.flatMap(_._2.asScala).filter(_.ts_code.equals(topInst.ts_code))
          if (ls.size > 0) {
            topInst.ts_name = ls.head.ts_name
          }
          val ls1 = AllStockController.hmDetailMap.flatMap(_._2.asScala).filter(_.hm_orgs.trim.equals(topInst.exalter.trim))
          if (ls1.size > 0) {
            topInst.hm_name = ls1.head.hm_name
          }
          else {
            topInst.hm_name = "unknow"
          }

          topInst
        })
        .toList
      in.close()
      val countMap = codes.map(e=>{
        if(e.side.equals("0")){
          e.side_desc = "买入"
        }
        else if(e.side.equals("1")) {
          e.side_desc = "卖出"
        }
        e
      }).groupBy(_.ts_code).map(e => (e._1, e._2.size))
      codes.foreach(c => {
        c.count = countMap.get(c.ts_code).get //计算买入的游资数量
      })
      topInstMap.put(tradedate, codes.sortBy(_.count).reverse.asJava)
    }

    val list = new util.ArrayList[TopInst]()
    if(topInstMap.get(tradedate).nonEmpty){
      list.addAll(topInstMap.get(tradedate).get.asScala.filter(e=>{
        scala.collection.mutable.ListBuffer(e.ts_code,e.ts_name,e.hm_name,e.exalter).filter(s=>s!=null && s.contains(search)).size>0
      }).asJava)
    }

    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", list)
    map
  }

}

