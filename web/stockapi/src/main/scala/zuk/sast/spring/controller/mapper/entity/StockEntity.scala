package zuk.sast.spring.controller.mapper.entity

import java.text.SimpleDateFormat
import java.util.Date
import scala.beans.BeanProperty

class StockEntity {

  @BeanProperty var id: String = null
  @BeanProperty var stockCode: String = null
  @BeanProperty var name: String = null
  @BeanProperty var stockType: String = null //类型： buy，attention， eliminate, MA4_MODEL
  @BeanProperty var createtime: String = null
  @BeanProperty var remark: String = null

}
