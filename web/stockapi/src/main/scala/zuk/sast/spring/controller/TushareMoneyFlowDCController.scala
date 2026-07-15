package zuk.sast.spring.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.spring.controller.component.{MoneyflowDCDto, TushareMoneyFlowComponent}

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
    val list = tushareMoneyFlowComponent.getTsCode(tsCode).toBuffer

    try {
      val dto = new MoneyflowDCDto
      dto.net_amount = list.map(_.net_amount.toFloat).sum.toString
      dto.buy_elg_amount = list.map(_.buy_elg_amount.toFloat).sum.toString
      dto.buy_lg_amount = list.map(_.buy_lg_amount.toFloat).sum.toString
      dto.buy_md_amount = list.map(_.buy_md_amount.toFloat).sum.toString
      dto.buy_sm_amount = list.map(_.buy_sm_amount.toFloat).sum.toString
      list += dto
    }
    catch {
      case exception: Exception =>
    }

    val result = new util.HashMap[String, Object]()
    result.put("data", list.asJava)
    result.put("code", "success")
    result
  }

}
