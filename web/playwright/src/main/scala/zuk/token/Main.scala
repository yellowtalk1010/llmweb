package zuk.token

import zuk.token.providers.ChromeBrowser
import zuk.token.providers.deepseek.DeepseekWeb

object Main {

  def main(args: Array[String]): Unit = {
    println("hello")
    val content = "用joern实现c/c++/joern的数据溢出问题"
    ChromeBrowser.chatList.clear()

    (ChromeBrowser.chatList ++= Array(new DeepseekWeb())).foreach(c=>{
      println(c.getClass.getName)
      c.chat(content)
    })
  }

}
