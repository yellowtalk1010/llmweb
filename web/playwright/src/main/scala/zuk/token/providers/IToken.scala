package zuk.token.providers

import com.microsoft.playwright.{Page, Request, Response, Route}

trait IToken {

  def llmName(): String //web llm 名称
  def llmChatURL(): String //聊天页面
  def llmOfficialWebsite(): String //官网地址

  def sayHi(): Unit
  def chat(content: String): Unit

  def onListenRequest(request: Request): Boolean
  def onListenRoute(route: Route): Boolean

  def delete(): Unit

}
