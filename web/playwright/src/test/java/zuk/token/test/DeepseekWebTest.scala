package zuk.token.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.token.TaskHandleFactory
import zuk.token.providers.ChromeBrowser

class DeepseekWebTest extends AnyFunSuite{

  test("deepseek测试"){

    TaskHandleFactory.initTask()
    ChromeBrowser.chatList.foreach(iProvider=>{
      val llmName = iProvider.llmName()
      println(llmName)
      println(iProvider.getClass.getSimpleName)
      iProvider.chat("你好")
      Thread.sleep(600 * 1000)
    })

  }

}
