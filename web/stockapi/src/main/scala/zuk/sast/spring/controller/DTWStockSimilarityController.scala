package zuk.sast.spring.controller

import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

import scala.collection.mutable.ListBuffer
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, IncreateDecreateRateDescUtil}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Random
import scala.jdk.CollectionConverters.*
import java.math.{BigDecimal, RoundingMode}
import zuk.similar.*

@RestController
@RequestMapping(value = Array("stock_similar"))
@Component
class DTWStockSimilarityController {

  private var trade_date = "999999999"

  //http://localhost:8080/stock_similar/getTsCode?tsCode=000001.SZ&tradeDate=20260924

  @GetMapping(value = Array("getTsCode"))
  def getTsCode(tsCode: String, tradeDate: String): java.util.Map[String, Object] = {
 
    val dto = SimilartyUtil.getTsCode(tsCode, tradeDate.replaceAll("-", ""))
    
    val result = new java.util.HashMap[String, Object]()
    result.put("data", dto)
    result.put("code", "success")
    result

  }

}
