package zuk.tu_share.dto

import scala.beans.BeanProperty

class MA {

  @BeanProperty var avg: String = _
  @BeanProperty var avg5: String = _
  @BeanProperty var avg10: String = _
  @BeanProperty var avg20: String = _
  @BeanProperty var avg30: String = _

  @BeanProperty var ma5: String = _ //过去5个交易日收盘价的平均值
  @BeanProperty var ma10: String = _
  @BeanProperty var ma20: String = _
  @BeanProperty var ma30: String = _

}
