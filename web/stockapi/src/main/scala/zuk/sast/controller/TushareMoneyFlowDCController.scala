package zuk.sast.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.controller.component.TushareMoneyFlowComponent

import java.util

import scala.jdk.CollectionConverters.*

/***
 * 股票资金流向
 */
@RestController
@RequestMapping(value = Array("money_flow"))
@Component
class TushareMoneyFlowDCController {

  private val log = LoggerFactory.getLogger(classOf[TushareMoneyFlowDCController])

  @Autowired
  private var tushareMoneyFlowComponent: TushareMoneyFlowComponent = null

  @GetMapping(value = Array("getTsCode"))
  def getTsCode(tsCode: String): util.Map[String, Object] = {
    log.info(s"查询：${tsCode}")
    val list = tushareMoneyFlowComponent.getTsCode(tsCode).asJava
    val result = new util.HashMap[String, Object]()
    result.put("data", list)
    result.put("code", "success")
    result
  }

}
