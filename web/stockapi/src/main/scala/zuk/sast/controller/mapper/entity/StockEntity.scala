package zuk.sast.controller.mapper.entity

import scala.beans.BeanProperty

class StockEntity {

  @BeanProperty var id: String = null
  @BeanProperty var stock_code: String = null
  @BeanProperty var name: String = null
  @BeanProperty var stock_type: String = null

}
