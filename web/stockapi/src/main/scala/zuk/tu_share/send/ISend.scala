package zuk.tu_share.send

import zuk.tu_share.module.StockDto

trait ISend {

  def doSend(list: List[StockDto]): Unit

}
