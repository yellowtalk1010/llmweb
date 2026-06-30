package zuk.token

//import zuk.token.providers.ChromeBrowser.chatList
import zuk.token.providers.deepseek.DeepseekWeb
import zuk.token.providers.kimi.KimiWeb
import zuk.token.providers.qianwen.QianwenWeb
import zuk.token.providers.tasks.ITask
import zuk.token.providers.{ChromeBrowser, IProviderToken}

import java.util
import scala.collection.mutable.ListBuffer

object TaskHandleFactory {

  /***
   * 任务队列
   */
  val TASK_QUEUE = new util.LinkedList[ITask]()

  def initTaskTest(list: List[IProviderToken]): Unit = {
    ChromeBrowser.init(list)
  }

  def initTask(): Boolean = {

    try {
      val array = Array(
        new DeepseekWeb,
        new QianwenWeb,
//        new KimiWeb
      )
      ChromeBrowser.init(array.toList)
      true
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        false
    }

  }


}
