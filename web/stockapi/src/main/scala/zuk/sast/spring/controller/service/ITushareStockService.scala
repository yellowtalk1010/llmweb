package zuk.sast.spring.controller.service

import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.TushareInitMA4ModelMA5ModelComponent

case class QuerTushareStockDto(desc: String,
                               status: String,
                               selectedDateStart: String,
                               selectedDateEnd: String
                              )

trait ITushareStockService {

  def getType(): String
  
  def getStocks(dto: QuerTushareStockDto = null): java.util.List[TushareStockControllerDTO]

  /** *
   * 获取购买股票
   *
   * @return
   */
  def getAllBuy(): Set[String] = {
    TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.buy_str)).map(_.stockCode).toSet
  }

  /** *
   * 获取关注股票
   *
   * @return
   */
  def getAllAttention(): Set[String] = {
    TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.attention_str)).map(_.stockCode).toSet
  }
  
}
