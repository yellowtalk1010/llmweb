package zuk.tu_share.dto

import scala.beans.BeanProperty

/***
 * 推荐结果
 */
class RecommendResult extends TsStock {

  @BeanProperty var modDesc: String = ""      //模型描述
  @BeanProperty var modClsName: String = ""   //模型名称

  @BeanProperty var modWinRate: String = ""   //模型胜率

  @BeanProperty var limitUp: String = ""
  @BeanProperty var limitDown: String = ""
  @BeanProperty var changeUpRate: String = ""

}
