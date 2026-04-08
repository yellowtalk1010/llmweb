package zuk.token

import zuk.token.providers.DeepseekWebAuth

object Main {

  def main(args: Array[String]): Unit = {
    println("hello")
    val dsWebAuth = new DeepseekWebAuth()
    dsWebAuth.webLogin()
  }

}
