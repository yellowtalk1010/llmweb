package zuk.token

//import zuk.token.providers.ChromeBrowser.chatList
import zuk.token.providers.deepseek.DeepseekWeb
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

  def initTask(): Unit = {

    val array = Array(new DeepseekWeb)
    ChromeBrowser.init(array.toList)

  }


}
