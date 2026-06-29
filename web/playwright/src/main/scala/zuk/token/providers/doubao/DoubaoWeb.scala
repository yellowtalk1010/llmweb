package zuk.token.providers.doubao

import com.microsoft.playwright.Request
import zuk.token.providers.IProviderToken

/***
 * 字节豆包
 */
class DoubaoWeb extends IProviderToken{

  override def llmName(): String = "doubao" //字节豆包

  override def llmChatURL(): String = "https://www.doubao.com/chat"

  override def chat(chatContent: String): Unit = {

  }

  override def onListenRequest(request: Request): Boolean = {
    true
  }

  override def delete(): Unit = {

  }

  override def parseProvider(responseText: String): String = {
    ""
  }

  override def run(): Unit = {

  }
}
