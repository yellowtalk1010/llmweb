package zuk.token.providers

import com.microsoft.playwright.{Browser, BrowserContext, Playwright}

import java.util
import java.util.List

import scala.jdk.CollectionConverters.*

object ChromeBrowser {

  private val playwright: Playwright = Playwright.create

  private val browser: Browser = playwright.chromium.connectOverCDP("http://127.0.0.1:9222")

  private val browserContexts: util.List[BrowserContext] = browser.contexts

  val browserContext: BrowserContext = if(browserContexts!=null && browserContexts.size()>0){
    browserContexts.asScala.head
  }
  else {
    null
  }

}
