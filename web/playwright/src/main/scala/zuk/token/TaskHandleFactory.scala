package zuk.token

import zuk.token.providers.ChromeBrowser
import zuk.token.providers.deepseek.tasks.ITask

import java.util

object TaskHandleFactory {

  /***
   * 任务队列
   */
  val TASK_QUEUE = new util.LinkedList[ITask]()

  def initTask(): Unit = {
    ChromeBrowser.init()
  }


}
