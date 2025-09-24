package zuk.sast.controller.response

import java.util
import scala.beans.BeanProperty

class JJQCResponse {
  @BeanProperty var code: String = _
  @BeanProperty var msg: String = _
  @BeanProperty var data: util.List[JJQCData] = new util.ArrayList[JJQCData]
}
