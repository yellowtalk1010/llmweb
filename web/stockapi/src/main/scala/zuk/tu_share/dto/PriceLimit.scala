package zuk.tu_share.dto

import java.math.BigDecimal
import scala.beans.BeanProperty

class PriceLimit {
  
  @BeanProperty var priceLimitUp: BigDecimal = _    //涨停价
  @BeanProperty var priceLimitDown: BigDecimal = _  //跌停价

}
