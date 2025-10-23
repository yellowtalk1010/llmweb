package zuk.jiucai

import scala.beans.BeanProperty

class Stock {
  @BeanProperty var stock_code = ""
  @BeanProperty val buy_count = 0
  @BeanProperty val buy_price = 0.0
  @BeanProperty var stock_name = ""
  @BeanProperty val stock_name_alias = ""
}
