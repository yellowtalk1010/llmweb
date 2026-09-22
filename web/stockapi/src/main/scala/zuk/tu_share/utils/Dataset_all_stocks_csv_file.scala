package zuk.tu_share.utils

import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.TsStock

import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

/***
 * 加载 all_stocks.csv 文件
 */
object Dataset_all_stocks_csv_file {

  private val tsStockList = ListBuffer[TsStock]()

  def getTsStock(tsCode: String): Option[TsStock] = {
    if (StringUtils.isEmpty(tsCode)) {
      Option.empty
    }
    else {
      val ls = Dataset_all_stocks_csv_file.load.filter(_.ts_code.equals(tsCode))
      if (ls.size > 0) {
        Some(ls.head)
      }
      else {
        Option.empty
      }
    }
  }

  /**
   * 加载 all_stocks.csv 中的数据
   *
   * @param all_stocks_csv
   * @return
   */
  def load: List[TsStock] = synchronized {

    if(tsStockList.size > 5000){
      return tsStockList.toList
    }

    val all_stocks_csv = ParseCammandParam.param.engineInfo.all_stocks_csv_file
    val all_stocks_file = new File(all_stocks_csv)
    println(s"加载all_stocks.csv文件，路径：${all_stocks_file.getAbsolutePath}，${all_stocks_file.exists()}")
    if (!all_stocks_file.exists() || !all_stocks_file.isFile) {
      System.exit(1)
    }
    //将tushare的csv数据转成对象
    val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val list = records.asScala.map(record => {
        //股票代码
        val stockCode = record.get("ts_code")
        val stockName = record.get("name")

        val tsStock = new TsStock(stockCode.trim, stockName.trim)
        tsStock.symbol = record.get("symbol")
        tsStock.area = record.get("area")
        tsStock.industry = record.get("industry")
        tsStock.market = record.get("market")

        tsStock
      })
      .toList
    in.close()
    println(s"${list.size}")

    tsStockList.clear()
    tsStockList ++= list

    tsStockList.toList
  }

}
