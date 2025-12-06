package zuk.run

import org.apache.commons.csv.CSVFormat
import zuk.stockapi.StockApiVo

import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.jdk.CollectionConverters.*

object DataFrame {

  var all_stocks_path = ""

  /***
   * 加载全部股票基本数据
   */
  private def loadAllStocks(path: String) = {
    all_stocks_path = path + File.separator + "all_stocks.csv"
    val all_stocks_file = new File(all_stocks_path)
    if (!all_stocks_file.exists() || !all_stocks_file.isFile) {
      println(s"${all_stocks_file.getAbsolutePath}不存在")
      System.exit(1)
    }
    //将tushare的csv数据转成对象
    val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record => {
        val ts_code = record.get("ts_code")
        val name = record.get("name")
        val splits = ts_code.split("\\.")//将数据中股票的代码拆分，如000001.SZ，拆分为000001，SZ
        val code = splits(0)
        val jys = splits(1)
        val gl = record.get("industry")
        val area = record.get("area")
        val stockApiVo = new StockApiVo()
        stockApiVo.setApi_code(code)  //股票代码
        stockApiVo.setName(name)      //股票名称
        stockApiVo.setJys(jys)        //SZ, SH, BJ
        stockApiVo.setGl(gl)          //股票所属行业
        stockApiVo.setArea(area)      //股票所在区域
        stockApiVo
      })
      .toList
    in.close()
    println(s"${codes.size}")

    codes
  }

  def load(path: String): Unit = {
    println(s"load data path:${path}")

    //判断路径是否存在
    val dir = new File(path)
    if (!dir.exists() || !dir.isDirectory) {
      println(s"${dir.getAbsolutePath}不存在")
      System.exit(1)
    }





  }

}
