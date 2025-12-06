package zuk.tu_share.dto

import scala.beans.BeanProperty

class ModuleDay {

  @BeanProperty var ts_code: String = _
  @BeanProperty var name: String = _
  @BeanProperty var trade_date: String = _
  @BeanProperty var open: String = _
  @BeanProperty var high: String = _
  @BeanProperty var low: String = _
  @BeanProperty var close: String = _
  @BeanProperty var pre_close: String = _
  @BeanProperty var change: String = _
  @BeanProperty var vol: String = _
  @BeanProperty var amount: String = _
  @BeanProperty var turnover_rate: String = _
  @BeanProperty var float_share: String = _

  //
  @BeanProperty var tsStock: TsStock = _
  @BeanProperty var ma: MA = _

}
