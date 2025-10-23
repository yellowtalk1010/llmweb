package zuk.jiucai.test

import zuk.jiucai.Window

import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

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



