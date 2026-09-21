package zuk.tu_share.utils

import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.{TopInst, TsStock}
import zuk.tu_share.module.MA8_Model

import java.io.{File, FileReader}
import java.nio.charset.Charset
import java.nio.file.Paths
import java.util
import java.util.List
import scala.collection.immutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

/***
 * 龙虎榜机构交易单
 */
object Dataset_top_Inst_dir {

  val SIZE = 20 //只考虑过去10个交易日的龙虎榜

  /***
   * String 格式 yyyyMMdd
   * 
   */
  private val topInstMap = java.util.HashMap[String, List[TopInst]]()


  /***
   * 是否出现在龙虎榜中
   * @param tscode
   * @return
   */
  def existTopInst(tscode: String, tradedate: String = null): String = {
    val ls = topInstMap.asScala.flatMap(_._2.asScala).filter(e=>{
      if(StringUtils.isBlank(tradedate)){
        e.ts_code.equals(tscode)
      }
      else {
        e.trade_date.equals(tradedate) && e.ts_code.equals(tscode)
      }
    })
    val groupLs = ls.groupBy(_.trade_date).toList.sortBy(e=>(e._1)).reverse
    //groupLs.map(_._1).foreach(println)
//    val size = groupLs.size
//    size
    if(groupLs.size==0){
      ""
    }
    else {
      s"上龙虎榜${groupLs.size}次最近${groupLs.head._1}"
    }
  }

  /***
   * 
   * @return
   */
  def load(): java.util.HashMap[String, List[TopInst]] = synchronized {
    
    if(topInstMap.size > 0){
      return topInstMap
    }
    //
    val topInstDirPath = Paths.get(ParseCammandParam.param.datasetInfo.top_inst_dir)
    val topInstFiles = new ListBuffer[File]
    topInstDirPath.toFile.listFiles().toList.sortBy(e => e.getName).reverse.foreach(yearDir => {
      for (f <- yearDir.listFiles().sortBy(_.getName).reverse if topInstFiles.size <= 100) {
        topInstFiles += f
      }
    })
    topInstFiles.foreach(file => {
      val topInstList = Dataset_top_Inst_dir.loadData(file)
      if(topInstList!=null && topInstList.size()>0){
        topInstMap.put(topInstList.asScala.head.trade_date, topInstList)
      }
    })
    topInstMap
  }


  private def loadData(csvFile: File): List[TopInst] = synchronized {
    try {
      val in = new FileReader(csvFile.getAbsolutePath, Charset.forName("UTF-8"))
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
          //股票名称
          topInst.ts_name = Dataset_all_stocks_csv_file.getTsStock(topInst.ts_code).getOrElse(new TsStock()).name 

          //获取机构获取，或者游资名称
          val ls1 = Dataset_hm_detail_dir.loadData().flatMap(_._2).filter(_.hm_orgs.trim.equals(topInst.exalter.trim)) 
          topInst.hm_name = if (ls1.size > 0) {
            ls1.head.hm_name
          }
          else {
            "unknow"
          }

          topInst.easyMoneyURL = new TsStock(topInst.ts_code).eastmoneyURL

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

      codes.sortBy(_.count).reverse.asJava
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        new util.ArrayList[TopInst]()

    }
  }

  private def getTradedate(filename: String): String = {
    filename.replace("_top_inst", "").replace(".csv","")
  }

}
