package zuk.sast.controller.mapper.entity

import java.text.SimpleDateFormat
import java.util.Date
import scala.beans.BeanProperty

class StockEntity {

  @BeanProperty var id: String = null
  @BeanProperty var stockCode: String = null
  @BeanProperty var name: String = null
  @BeanProperty var stockType: String = null
  @BeanProperty var createtime: String = null

}
