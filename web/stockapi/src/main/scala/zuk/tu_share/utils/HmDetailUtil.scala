package zuk.tu_share.utils

import org.apache.commons.csv.CSVFormat
import zuk.tu_share.dto.HmDetail

import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.jdk.CollectionConverters.*

object HmDetailUtil {


  val SIZE = 30

  val hmDetailMap = scala.collection.mutable.HashMap[String, List[HmDetail]]()

  /***
   * 游资交易每日明细
   *
   * 最近30天
   */
  def loadData(): Unit = {
    if(hmDetailMap.size>0){
      return
    }
    val hmDetailPath = "tushare/hm/hm_detail/"
    val hmDetailFile = new File(hmDetailPath)
    var files = hmDetailFile.listFiles().sortBy(_.getName).reverse
    if (files.size>SIZE){
      files = files.take(SIZE)
    }
    files.map(_.getAbsolutePath).foreach(println)
    files.foreach(hmFile=>{
      val tradedate = getTradedate(hmFile.getName)

      val in = new FileReader(hmFile.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      val codes = records.asScala.map(record => {
          val hmDetail = new HmDetail()
          hmDetail.trade_date = record.get("trade_date")
          hmDetail.ts_code = record.get("ts_code")
          hmDetail.ts_name = record.get("ts_name")
          hmDetail.buy_amount = record.get("buy_amount").toFloat.toString
          hmDetail.sell_amount = record.get("sell_amount").toFloat.toString
          hmDetail.net_amount = record.get("net_amount").toFloat.toString
          hmDetail.hm_name = record.get("hm_name")
          hmDetail.hm_orgs = record.get("hm_orgs")
          //
          hmDetail.splitTsCode(hmDetail.ts_code)
          hmDetail
        })
        .toList
      in.close()

      val countMap = codes.groupBy(_.ts_code).map(e => (e._1, e._2.size))
      codes.foreach(c => {
        c.count = countMap.get(c.ts_code).get //计算买入的游资数量
      })

      hmDetailMap.put(tradedate, codes.sortBy(_.count).reverse)

    })

  }

  private def getTradedate(filename: String): String = {
    filename.replace("hm_detail-", "").replace(".csv","")
  }
}
