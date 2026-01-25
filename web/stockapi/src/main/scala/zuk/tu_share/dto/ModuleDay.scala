package zuk.tu_share.dto

import scala.beans.BeanProperty

object ModuleDay {
  val N = "N" //默认
  val U = "U" //涨停
  val D = "D" //跌停
  val Z = "Z" //炸板
}

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
  @BeanProperty var total_mv: String = _
  @BeanProperty var limit: String = ModuleDay.N   //D跌停，U涨停，Z炸板

  @BeanProperty var ma: MA = _
  @BeanProperty var priceLimit: PriceLimit = _

}
