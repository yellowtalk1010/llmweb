package zuk.token.providers

import com.microsoft.playwright.{Page, Request, Response, Route}
import zuk.token.providers.deepseek.tasks.ITask

trait IToken extends Runnable{

  def llmName(): String //web llm 名称
  def llmChatURL(): String //聊天页面
  def llmOfficialWebsite(): String //官网地址

  def chat(chatContent: String): Unit

  def onListenRequest(request: Request): Boolean
  def onListenRoute(route: Route): Boolean

  def delete(): Unit

}
