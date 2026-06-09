package zuk.sast.controller.mapper.entity

import java.text.SimpleDateFormat
import java.util.Date
import scala.beans.BeanProperty

class StockEntity {

  @BeanProperty var id: String = null
  @BeanProperty var stockCode: String = null
  @BeanProperty var name: String = null
  @BeanProperty var stockType: String = null //类型： buy，attention， eliminate
  @BeanProperty var createtime: String = null

}
