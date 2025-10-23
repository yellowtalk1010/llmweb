package zuk.jiucai.test

object ConfigUtil {

  def createConfigJsonFile(code_names: List[(String, String)]): Config = {
    val config = new Config
    code_names.map(e => {
      val stock = new Stock
      stock.stock_code = e._1
      stock.stock_name = e._2
      stock
    }).foreach(config.stocks.add)
    config
  }

}
