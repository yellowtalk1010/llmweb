package zuk.tu_share.utils

import org.apache.commons.csv.CSVFormat
import zuk.tu_share.dto.HmDetail

import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.jdk.CollectionConverters.*

object HmDetailUtil {

  val hmDetailMap = scala.collection.mutable.HashMap[String, List[HmDetail]]()

  def loadData(): Unit = {
    if(hmDetailMap.size>0){
      return
    }
    val hmDetailPath = "tushare/hm/hm_detail/"
    val hmDetailFile = new File(hmDetailPath)
    var files = hmDetailFile.listFiles().sortBy(_.getName).reverse
    if (files.size>30){
      files = files.take(30)
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

      hmDetailMap.put(tradedate, codes)

    })

  }

  private def getTradedate(filename: String): String = {
    filename.replace("hm_detail-", "").replace(".csv","")
  }
}
