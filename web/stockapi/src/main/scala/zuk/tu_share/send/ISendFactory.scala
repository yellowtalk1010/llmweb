package zuk.tu_share.send

import zuk.tu_share.module.IModel

object ISendFactory {

  def doSend(list: List[IModel]): Unit = {
    try {
      sendList.foreach(_.doSend(list))
    }
    catch
      case exception: Exception =>
  }

  private def sendList: List[ISend] = {
    List(
      new Console,
      new Email,
      new JsonFile,
      new ToBackTest,
    )
  }

}
