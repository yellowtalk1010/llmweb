package zuk.token.providers

import com.microsoft.playwright.Page

trait IToken {

  def llmName(): String

  def llmChatURL(): String

  /***
   * 在聊天页面发送 say hi
   */
  def sayHi(): Unit

  def webLogin(): Unit

  def chat(content: String): Unit

  def delete(): Unit

}
