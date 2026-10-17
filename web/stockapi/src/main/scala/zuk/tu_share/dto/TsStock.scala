package zuk.tu_share.dto

import scala.beans.BeanProperty

/***
 * tuShare 股票信息
 *
 * @param ts_code tushare股票代码
 */
class TsStock(@BeanProperty var ts_code: String) extends TsCodeSplit{
  
  splitTsCode(this.ts_code)

  @BeanProperty var symbol: String = _      //股票代码
  @BeanProperty var name: String = _        //名称
  @BeanProperty var area: String = _        //地域
  @BeanProperty var industry: String = _    //所属行业
  @BeanProperty var market: String = _      //市场类型（主板/创业板/科创板/CDR）

  @BeanProperty var eastmoneyURL: String = ""     //东方财富跳转url
  @BeanProperty var conceptURL: String = ""       //东方财富股票所属概念url
  @BeanProperty var zjlxURL: String = ""          //东方财务资金流向url

  //这里相当于创建类时的初始化
  this.splitTsCode(ts_code)
  this.eastmoneyURL = createEastmoneyURL()
  this.conceptURL = createConceptURL()
  this.zjlxURL = create_ZJLX_URL()

  def this() = {
    this("")
    name = ""
  }

//  def this(@BeanProperty stockCode: String) = {
//    this()
//    this.ts_code = stockCode
//    super.splitTsCode(ts_code)
//    this.eastmoneyURL = createEastmoneyURL()
//    this.conceptURL = createConceptURL()
//  }

  def this(@BeanProperty stockCode: String,
           @BeanProperty stockName: String) = {
    this(stockCode)
    this.name = stockName
  }

//  /***
//   * 更新最新名字
//   * @return
//   */
//  def lastestName(): TsStock = {
//    val optStock = TushareAllStocks.getTsStock(this.ts_code)
//    if(!optStock.isEmpty){
//      this.name = optStock.get.name
//    }
//    this
//  }

  private def createEastmoneyURL(): String = {
    try {
      this.eastmoneyURL = if (s_SZ_or_SH.toUpperCase.contains("BJ")) {
        s"https://quote.eastmoney.com/${s_SZ_or_SH}/${s_0}.html"
      }
      else {
        s"https://quote.eastmoney.com/${s_SZ_or_SH}${s_0}.html"
      }
    }
    catch {
      case exception: Exception =>
    }
    this.eastmoneyURL
  }

  private def createConceptURL(): String = {
    try {
      val code: String = s"${s_SZ_or_SH}${s_0}"
      this.conceptURL = s"https://emweb.securities.eastmoney.com/pc_hsf10/pages/index.html?type=web&code=${code}&color=b#/hxtc/tcxq"
    }
    catch {
      case exception: Exception =>
    }
    this.conceptURL
  }
  
  private def create_ZJLX_URL(): String = {
    try {
      this.zjlxURL = s"https://data.eastmoney.com/zjlx/${s_0}.html"
    }
    catch {
      case exception: Exception =>
    }
    this.zjlxURL
  }



}

