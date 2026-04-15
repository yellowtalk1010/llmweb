package zuk.token.providers

import com.microsoft.playwright.{Page, Request, Route}

trait IToken {

  def llmName(): String //web llm 名称
  def llmChatURL(): String //聊天页面
  def llmOfficialWebsite(): String //官网地址

  def sayHi(): Unit
  def chat(content: String): Unit

  def onListen(route: Route): Boolean
//  def webLogin(): Unit

  def delete(): Unit

}
