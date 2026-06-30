package zuk.token.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.token.TaskHandleFactory
import zuk.token.providers.qianwen.QianwenWeb

import java.util.UUID

class QianwenTest extends AnyFunSuite{


  test("qianwen"){
    TaskHandleFactory.initTaskTest(Array(new QianwenWeb).toList)

    val t1 = new TaskTest
    t1.id = UUID.randomUUID().toString.replaceAll("-", "")
    t1.chatContent = "你好，请介绍一下你自己"
    TaskHandleFactory.TASK_QUEUE.push(t1)

    val t2 = new TaskTest
    t2.id = UUID.randomUUID().toString.replaceAll("-", "")
    t2.chatContent = "你好，请介绍一下你自己"
    TaskHandleFactory.TASK_QUEUE.push(t2)


    Thread.sleep(999999)
  }

}
