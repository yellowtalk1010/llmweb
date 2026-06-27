package zuk.token

import zuk.token.providers.ChromeBrowser
import zuk.token.providers.deepseek.DeepseekWeb

object Main {

  def main(args: Array[String]): Unit = {
    println("hello")
    val content = "joern可以开发Misra2012吗？"
    ChromeBrowser.chatList.clear()

    (ChromeBrowser.chatList ++= Array(new DeepseekWeb())).foreach(c=>{
      println(c.getClass.getName)
      c.chat(content)
    })
  }


  /***
   * ChromeBrowser.browserContext.onRequest(handle=>{
   * println("onrequest:" + handle.url())
   * if(handle.url().contains("/completion")){
   * val response = handle.response()
   * val text = response.text()
   * println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv")
   * println(s"text:${text}")
   * println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
   *
   * }
   * })
   */
}
