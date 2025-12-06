package zuk.tu_share.dto

import java.math.BigDecimal
import scala.beans.BeanProperty

class MA {

  @BeanProperty var avg: BigDecimal = _
  @BeanProperty var avg5: BigDecimal = _
  @BeanProperty var avg10: BigDecimal = _
  @BeanProperty var avg20: BigDecimal = _
  @BeanProperty var avg30: BigDecimal = _

  @BeanProperty var ma5: BigDecimal = _ //过去5个交易日收盘价的平均值
  @BeanProperty var ma10: BigDecimal = _
  @BeanProperty var ma20: BigDecimal = _
  @BeanProperty var ma30: BigDecimal = _

}
