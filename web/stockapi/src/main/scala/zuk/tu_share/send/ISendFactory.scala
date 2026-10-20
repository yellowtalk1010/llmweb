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
      //todo 必须执行
      new Console,
      new ToBackTest,
      
      //todo 通过参数控制
      new Email,
      new JsonFile,
    )
  }

}
