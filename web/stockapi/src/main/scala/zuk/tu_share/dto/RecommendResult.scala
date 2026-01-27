package zuk.tu_share.dto

import java.util
import scala.beans.BeanProperty

/***
 * 推荐结果
 */
object RecommendResult{
  @BeanProperty val result: java.util.List[RecommendResult] = new util.ArrayList[RecommendResult]()
}
class RecommendResult extends TsStock {

  @BeanProperty var modDesc: String = ""      //模型描述
  @BeanProperty var modClsName: String = ""   //模型名称

  @BeanProperty var modWinRate: String = ""   //模型胜率

  @BeanProperty var limitUp: String = ""
  @BeanProperty var limitDown: String = ""
  @BeanProperty var changeUpRate: String = ""

}
