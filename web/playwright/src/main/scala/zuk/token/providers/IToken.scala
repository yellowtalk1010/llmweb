package zuk.token.providers

import com.microsoft.playwright.{Page, Request, Response, Route}

trait IToken extends Runnable{

  def llmName(): String //web llm 名称
  def llmChatURL(): String //聊天页面
  def llmOfficialWebsite(): String //官网地址

  def chat(chatContent: String): Unit

  def onListenRequest(request: Request): Boolean

  @Deprecated
  def onListenRoute(route: Route): Boolean

  def delete(): Unit

}
