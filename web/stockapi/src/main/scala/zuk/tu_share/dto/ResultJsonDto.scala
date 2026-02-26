package zuk.tu_share.dto

import scala.beans.BeanProperty

class ResultJsonDto {

  @BeanProperty var area: String = ""
  @BeanProperty var modDesc: String = ""
  @BeanProperty var ts_code: String = ""
  @BeanProperty var turnoverRate: String = ""
  @BeanProperty var name: String = ""

  @BeanProperty var limitUp: String = ""
  @BeanProperty var industry: String = ""
  @BeanProperty var limitDown: String = ""
  @BeanProperty var modWinRate: String = ""
  @BeanProperty var modClsName: String = ""

}


