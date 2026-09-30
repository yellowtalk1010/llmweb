package zuk.sast.spring.controller

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.similar.*

/***
 * 1、 样本足够足够多，50以上
 * 2、 样本的距离值足够小；
 * 3、 胜率 95%以上
 */
@RestController
@RequestMapping(value = Array("stock_similar"))
@Component
class DTWStockSimilarityController {

  private val trade_date = "999999999"
  
  private val log = LoggerFactory.getLogger(classOf[DTWStockSimilarityController])

  //http://localhost:8080/stock_similar/getTsCode?tsCode=000001.SZ&tradeDate=20260924

  /**
   * 
   * @param tsCode 股票
   * @param tradeDate 交易日期
   * @param rtk 是否带rtk计算，默认true
   * @return
   */
  @GetMapping(value = Array("getTsCode"))
  def getTsCode(tsCode: String, tradeDate: String, rtk: Boolean = true): java.util.Map[String, Object] = {
    val date = if(StringUtils.isNotBlank(tradeDate)){
      tradeDate
    }
    else {
      trade_date
    }
    
    log.info(s"tsCode:${tsCode}, tradeDate:${tradeDate}, rtk:${rtk}")
    
    val dto = SimilartyUtil.getTsCode(tsCode, date.replaceAll("-", ""))

    val result = new java.util.HashMap[String, Object]()
    result.put("data", dto)
    result.put("code", "success")
    result

  }

}
