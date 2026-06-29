package zuk.token.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.token.TaskHandleFactory
import zuk.token.providers.ChromeBrowser
import zuk.token.providers.deepseek.DeepseekWeb
import zuk.token.providers.doubao.DoubaoWeb
import zuk.token.providers.tasks.ITask

import java.util.UUID

class DoubaoTest extends AnyFunSuite{

  /***
   * 会丢失数据，问题未找到，所以时发送两次
   */
  test("deepseek测试"){

    TaskHandleFactory.initTaskTest(Array(new DoubaoWeb).toList)

    val t1 = new TaskTest
    t1.id = UUID.randomUUID().toString.replaceAll("-", "")
    t1.chatContent = "你好"
    TaskHandleFactory.TASK_QUEUE.push(t1)

    val t2 = new TaskTest
    t2.id = UUID.randomUUID().toString.replaceAll("-", "")
    t2.chatContent = "你好"
    TaskHandleFactory.TASK_QUEUE.push(t2)


    Thread.sleep(999999)

  }

}
