package zuk.sast.spring.controller.mapper.entity

import scala.beans.BeanProperty

class StockInfoEntity {

  @BeanProperty var id: String = null
  @BeanProperty var stockCode: String = null
  @BeanProperty var stockName: String = null
  @BeanProperty var concept: String = null

}
