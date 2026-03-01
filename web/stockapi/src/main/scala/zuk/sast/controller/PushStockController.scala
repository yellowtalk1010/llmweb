package zuk.sast.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

import java.util
import scala.beans.BeanProperty

/***
 * 股票推荐列表
 */
@RestController
@RequestMapping(value = Array("push_stocks"))
@Component
class PushStockController {

  private val log = LoggerFactory.getLogger(classOf[PushStockController])

  /***
   * 获取application.properties中的数据，股票json结果路径
   */
  @Value("${stock.result.json.path}")
  @BeanProperty
  private var stockResultJsonPath: String = null

  @GetMapping(value = Array("list"))
  def all(search: String, tradedate: String): util.Map[String, Object] = {

    val pro = System.getProperties
    log.info(s"股票json结果路径：${this.stockResultJsonPath}")

    val list = new util.ArrayList[Object]()
    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", list)
    map
  }
}
