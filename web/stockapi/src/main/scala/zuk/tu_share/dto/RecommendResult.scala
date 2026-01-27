package zuk.tu_share.dto

import zuk.tu_share.module.StockDto

import java.util
import scala.beans.BeanProperty

/***
 * 推荐结果
 */
object RecommendResult{
  @BeanProperty val results: java.util.List[StockDto] = new util.ArrayList[StockDto]()
}
