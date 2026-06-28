package zuk.token.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.token.TaskHandleFactory
import zuk.token.providers.{ChromeBrowser, ITask}

class DeepseekWebTest extends AnyFunSuite{

  test("deepseek测试"){

    TaskHandleFactory.initTask()

    val t1 = new TaskTest
    t1.chatContent = "你好"
    TaskHandleFactory.TASK_QUEUE.push(t1)

    val t2 = new TaskTest
    t2.chatContent = "你好"
    TaskHandleFactory.TASK_QUEUE.push(t2)


    Thread.sleep(999999)

  }

}
