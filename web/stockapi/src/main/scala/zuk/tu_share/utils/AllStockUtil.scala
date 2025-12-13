package zuk.tu_share.utils

import org.apache.commons.csv.CSVFormat
import zuk.tu_share.dto.TsStock

import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*


object AllStockUtil {

  private val STOCKS = new ListBuffer[TsStock]()

  /** *
   * 加载市场全部股票数据
   */
  def loadData(): List[TsStock] = synchronized {
    if(STOCKS.size>0){
      return STOCKS.toList
    }
    val all_stocks_path = "tushare/all_stocks.csv"
    val all_stocks_file = new File(all_stocks_path)

    println(s"${all_stocks_file.getAbsolutePath}，${all_stocks_file.exists()}")
    if (!all_stocks_file.exists() || !all_stocks_file.isFile) {
      System.exit(1)
    }
    //将tushare的csv数据转成对象
    val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record => {
        val tsStock = new TsStock()
        tsStock.ts_code = record.get("ts_code")
        tsStock.symbol = record.get("symbol")
        tsStock.name = record.get("name")
        tsStock.area = record.get("area")
        tsStock.industry = record.get("industry")
        tsStock.market = record.get("market")

        //额外处理
        tsStock.splitTsCode(tsStock.ts_code)
        tsStock
      })
      .toList
    in.close()
    println(s"${codes.size}")

    STOCKS ++= codes

    STOCKS.toList

  }

}
