package zuk.token

import zuk.token.providers.deepseek.DeepseekWebAuth

object Main {

  def main(args: Array[String]): Unit = {
    println("hello")
    val dsWebAuth = new DeepseekWebAuth()
    dsWebAuth.chat("用joern实现c/c++/joern的数据溢出问题")
  }

}
