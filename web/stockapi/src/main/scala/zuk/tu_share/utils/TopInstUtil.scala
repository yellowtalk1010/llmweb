package zuk.tu_share.utils

import org.apache.commons.csv.CSVFormat
import zuk.tu_share.dto.TopInst

import java.io.{File, FileReader}
import java.nio.charset.Charset
import java.util.List

import scala.jdk.CollectionConverters.*

object TopInstUtil {

  val SIZE = 30

  private val topInstMap = scala.collection.mutable.HashMap[String, List[TopInst]]()

  /***
   * 龙虎榜机构交易单
   *
   * 最近30天
   */
  def loadData(): scala.collection.mutable.HashMap[String, List[TopInst]] = synchronized {
    if(topInstMap.size>0){
      return topInstMap
    }
    val topInstPath = s"tushare/hm/top_inst/"
    val topInstFile = new File(topInstPath)
    var files = topInstFile.listFiles().sortBy(_.getName).reverse
    if (files.size > SIZE) {
      files = files.take(SIZE)
    }
    files.map(_.getAbsolutePath).foreach(println)
    files.foreach(topInstFile=>{
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
          topInst.splitTsCode(topInst.ts_code)
          val ls = HmDetailUtil.loadData().flatMap(_._2).filter(_.ts_code.equals(topInst.ts_code))
          if (ls.size > 0) {
            topInst.ts_name = ls.head.ts_name
          }
          val ls1 = HmDetailUtil.loadData().flatMap(_._2).filter(_.hm_orgs.trim.equals(topInst.exalter.trim))
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
      val countMap = codes.map(e => {
        if (e.side.equals("0")) {
          e.side_desc = "买入"
        }
        else if (e.side.equals("1")) {
          e.side_desc = "卖出"
        }
        e
      }).groupBy(_.ts_code).map(e => (e._1, e._2.size))
      codes.foreach(c => {
        c.count = countMap.get(c.ts_code).get //计算买入的游资数量
      })

      val tradedate = getTradedate(topInstFile.getName)
      topInstMap.put(tradedate, codes.sortBy(_.count).reverse.asJava)
    })

    topInstMap
  }



  private def getTradedate(filename: String): String = {
    filename.replace("_top_inst", "").replace(".csv","")
  }

}
