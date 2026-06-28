package zuk.token.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.token.TaskHandleFactory
import zuk.token.providers.{ChromeBrowser, ITask}

class TaskTest extends ITask {

  /**   *
   * 将deepseek、gpt进行解析
   *
   * @return
   */
  override def parseProvider(): String = ""

  /** *
   * 检测结果
   *
   * @return
   */
  override def checkResult(): Boolean = true
}

class DeepseekWebTest extends AnyFunSuite{

  test("deepseek测试"){


    val t1 = new TaskTest
    t1.chatContent = "你好"
    TaskHandleFactory.TASK_QUEUE.push(t1)

    val t2 = new TaskTest
    t2.chatContent = "你好"
    TaskHandleFactory.TASK_QUEUE.push(t2)


    TaskHandleFactory.initTask()

    Thread.sleep(999999)

  }

}
