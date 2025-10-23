package zuk.jiucai.test


import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

object Config {

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

class Config {
  @BeanProperty val log_level = "INFO"
  @BeanProperty val window = new Window()
  @BeanProperty val text_opacity = 168
  @BeanProperty val rest_reminder = new RestReminder
  @BeanProperty val profit_reminder_enabled = false
  @BeanProperty val show_profit_info = false
  @BeanProperty val show_stock_code = true
  @BeanProperty val stocks = List[Stock]().asJava
}

class RestReminder {
  @BeanProperty val enabled = false
  @BeanProperty val hours = List(10, 11, 14, 16).asJava.toArray
}

class Stock {
  @BeanProperty var stock_code = ""
  @BeanProperty val buy_count = 0
  @BeanProperty val buy_price = 0.0
  @BeanProperty var stock_name = ""
  @BeanProperty val stock_name_alias = ""
}

class Window {
  @BeanProperty val x = 337
  @BeanProperty val y = 973
}

