package zuk.token

import zuk.token.providers.{ChromeBrowser, ITask}

import java.util
import scala.collection.mutable.ListBuffer

object TaskHandleFactory {

  /***
   * 任务队列
   */
  val TASK_QUEUE = new util.LinkedList[ITask]()

  /***
   * 答案列表
   */
  val ANSWER_LIST = new ListBuffer[String]()


  /***
   * 已经完结的任务队列
   */
//  val TASK_FINISH = new util.LinkedList[ITask]()

  def initTask(): Unit = {
    ChromeBrowser.init()
  }


}
