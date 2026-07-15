package zuk.tu_share.utils

import zuk.tu_share.dto.TsStock

object All_stocks_csv_file_Util {

  /**
   * 加载 all_stocks.csv 中的数据
   *
   * @param all_stocks_csv
   * @return
   */
  def load(all_stocks_csv: String = "all_stocks.csv"): List[TsStock]  = {
    val all_stocks_file = new File(all_stocks_csv)
    println(s"加载all_stocks.csv文件，路径：${all_stocks_file.getAbsolutePath}，${all_stocks_file.exists()}")
    if (!all_stocks_file.exists() || !all_stocks_file.isFile) {
      System.exit(1)
    }
    //将tushare的csv数据转成对象
    val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record => {
        //股票代码
        val stockCode = record.get("ts_code")
        val stockName = record.get("name")

        val tsStock = new TsStock(stockCode, stockName)
        tsStock.symbol = record.get("symbol")
        tsStock.area = record.get("area")
        tsStock.industry = record.get("industry")
        tsStock.market = record.get("market")

        tsStock
      })
      .toList
    in.close()
    println(s"${codes.size}")

    codes
  }

}
