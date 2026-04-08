package zuk.token.providers

import com.microsoft.playwright.Page

import scala.jdk.CollectionConverters.*

class DeepseekWebAuth {

  val deepseekWebChatURL: String = "https://chat.deepseek.com"

  def webLogin(): Unit = {
    val browserContext = ChromeBrowser.browserContext
    if(browserContext == null){
      return
    }
    val exist = browserContext.pages().asScala.map(_.url()).toSet.filter(_.startsWith(deepseekWebChatURL)).size > 0
    var deepseekPage: Page = null
    if(exist){
      //如果存在，则输出全部url
      browserContext.pages().asScala.foreach(page => {
        val url = page.url()
        println(s"url:${url}")
        if(deepseekPage==null && page.url().startsWith(deepseekWebChatURL)){
          deepseekPage = page
        }
      })
    }
    else {
      //创建一个新页面
      deepseekPage = browserContext.newPage()
      //导航到登录页面
      deepseekPage.navigate(deepseekWebChatURL)
    }
    

  }

}
