package zuk.token.providers.chatGPT

import com.microsoft.playwright.{Request, Route}
import zuk.token.providers.IProviderToken

/***
 * 
 */
class ChatGPTWeb extends IProviderToken {

  override def llmName(): String = "chatGPT"

  override def llmChatURL(): String = "https://chatgpt.com/"
  
  override def chat(chatContent: String): Unit = {

  }

  override def onListenRequest(request: Request): Boolean = {
    true
  }

  override def delete(): Unit = {

  }

  override def run(): Unit = {

  }


  def parseProvider(responseText: String): String = {
    ""
  }
  
}
