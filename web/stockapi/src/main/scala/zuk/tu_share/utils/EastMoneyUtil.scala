package zuk.tu_share.utils

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.dto.TsCodeSplit

object EastMoneyUtil {

  /***
   * 创建东方财富url链接
   *
   * @param codeSplit
   * @return
   */
  def createURL(codeSplit: TsCodeSplit): String = {
    if(codeSplit.s_1.toUpperCase.equals("BJ")){
      s"https://quote.eastmoney.com/bj/${codeSplit.s_0}.html"
    }
    else {
      s"https://quote.eastmoney.com/${codeSplit.s_1}${codeSplit.s_0}.html"
    }
  }

  def createLocalURL(tsName: String, tradedate: String=""): String = {
    //http://localhost:8080/pages/allStock
    if(StringUtils.isBlank(tradedate)){
      s"http://localhost:8080/pages/allStock?search=${tsName}"
    }
    else {
      s"http://localhost:8080/pages/allStock?search=${tsName}&tradedate=${tradedate}"
    }

  }

}
