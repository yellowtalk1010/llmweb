package zuk.sast.controller.mapper.entity

import scala.beans.BeanProperty

class StockEntity {

  @BeanProperty var id: String = null
  @BeanProperty var stockCode: String = null
  @BeanProperty var name: String = null
  @BeanProperty var stockType: String = null

}
