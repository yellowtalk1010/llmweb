package zuk.token

import zuk.token.providers.ChromeBrowser
import zuk.token.providers.deepseek.DeepseekWeb
import zuk.token.providers.deepseek.tasks.DeepseekTask_easymoneyConcept

object Main {

  def main(args: Array[String]): Unit = {
    println("hello")
    val content: String = {
      """
        |提取 https://emweb.securities.eastmoney.com/pc_hsf10/pages/index.html?type=web&code=SH688323&color=b#/hxtc/tcxq 里面的板块和概念。输出的格式严格采用如下格式：
        |```
        |##########
        |股票分析结果如下：
        |股票代码：SH688323
        |股票名称：瑞华泰
        |概念：概念1，概念2
        |一级行业：行业1
        |二级行业：行业2
        |三级行业: 行业3
        |##########
        |```
        |不需要附加任何其他信息的补充说明。
        |""".stripMargin
//    ChromeBrowser.chatList.clear()
//
//    (ChromeBrowser.chatList ++= Array(new DeepseekWeb())).foreach(c=>{
//      println(c.getClass.getName)
//      for (i <- 0 until 2) {
//        val task = new DeepseekTask_easymoneyConcept(content)
//        c.chat(content)
//        Thread.sleep(10000)
//      }
//    })

    }
    try {
      TaskHandleFactory.initTask()
      val task = new DeepseekTask_easymoneyConcept()
      task.chatContent = content
      TaskHandleFactory.TASK_QUEUE.push(task)
      Thread.sleep(99999)
    }
    catch {
      case exception: Exception=>
    }



  }


  /***
   * ChromeBrowser.browserContext.onRequest(handle=>{
   * println("onrequest:" + handle.url())
   * if(handle.url().contains("/completion")){
   *  val response = handle.response()
   *  val text = response.text()
   *  println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv")
   *  println(s"text:${text}")
   *  println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
   *
   * }
   * })
   */
}
