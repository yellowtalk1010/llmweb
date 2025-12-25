package zuk.tu_share.module

import zuk.tu_share.dto.TsStock

import scala.beans.BeanProperty

class StockDto {

  @BeanProperty var limitUp: Boolean = false
  @BeanProperty var tsStock: TsStock = _
}
