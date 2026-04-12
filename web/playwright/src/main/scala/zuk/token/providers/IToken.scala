package zuk.token.providers

trait IToken {

  def sayHi(): Unit

  def webLogin(): Unit

  def chat(content: String): Unit

}
