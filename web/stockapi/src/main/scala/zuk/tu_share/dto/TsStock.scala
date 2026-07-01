package zuk.tu_share.dto

import zuk.sast.controller.component.{TushareAllStocks, TushareAllStocksCSVComponent}

import scala.beans.BeanProperty

/***
 * tuShare 股票信息
 */
class TsStock extends TsCodeSplit{

  @BeanProperty var ts_code: String = _     //ts代码
  @BeanProperty var symbol: String = _      //股票代码
  @BeanProperty var name: String = _        //名称
  @BeanProperty var area: String = _        //地域
  @BeanProperty var industry: String = _    //所属行业
  @BeanProperty var market: String = _      //市场类型（主板/创业板/科创板/CDR）

  var eastmoneyURL: String = ""     //东方财富跳转url
  var conceptURL: String = ""       //东方财富股票所属概念url

  def this(@BeanProperty stockCode: String) = {
    this()
    this.ts_code = stockCode
    super.splitTsCode(ts_code)
    this.eastmoneyURL = getEastmoneyURL()
    this.conceptURL = getConceptURL()

  }

  def this(@BeanProperty stockCode: String,
           @BeanProperty stockName: String) = {
    this(stockCode)
    this.name = stockName
  }

  /***
   * 更新最新名字
   * @return
   */
  def updateLastestName(): TsStock = {
    val optStock = TushareAllStocks.getTsStock(this.ts_code)
    if(!optStock.isEmpty){
      this.name = optStock.get.name
    }
    this
  }

  private def getEastmoneyURL(): String = {
    try {
      val splits = this.ts_code.split("\\.")
      this.eastmoneyURL = if (splits(1).toUpperCase.contains("BJ")) {
        s"https://quote.eastmoney.com/${splits(1)}/${splits(0)}.html"
      }
      else {
        s"https://quote.eastmoney.com/${splits(1)}${splits(0)}.html"
      }
      this.eastmoneyURL
    }
    catch {
      case exception: Exception => this.eastmoneyURL
    }
  }

  private def getConceptURL(): String = {
    try {
      splitTsCode(this.ts_code)
      val code: String = s"${s_1}${s_0}"
      this.conceptURL = s"https://emweb.securities.eastmoney.com/pc_hsf10/pages/index.html?type=web&code=${code}&color=b#/hxtc/tcxq"
      this.conceptURL
    }
    catch {
      case exception: Exception => this.conceptURL
    }
  }



}

