package zuk.token.providers.gemini

import com.microsoft.playwright.Request
import zuk.token.providers.IProviderToken

/***
 * google gemini
 */
class GeminiWeb extends IProviderToken {

  override def llmName(): String = "gemini"

  override def llmChatURL(): String = "https://gemini.google.com/app"

  override def chat(chatContent: String): Unit = {

  }

  override def onListenRequest(request: Request): Boolean = false

  override def delete(): Unit = {
    //
  }

  override def parseProvider(responseText: String): String = {
    ""
  }

  override def run(): Unit = {
    //
  }
}
