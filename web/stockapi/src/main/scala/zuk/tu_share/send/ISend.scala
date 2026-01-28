package zuk.tu_share.send

import zuk.tu_share.module.IModel

trait ISend {

  def doSend(list: List[IModel]): Unit

}
