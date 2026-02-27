package zuk.sast.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import java.util

/***
 * 股票推荐列表
 */
@RestController
@RequestMapping(value = Array("push_stocks"))
class PushStockController {

  private val log = LoggerFactory.getLogger(classOf[PushStockController])

  @GetMapping(value = Array("list"))
  def all(search: String, tradedate: String): util.Map[String, Object] = {

    val list = new util.ArrayList[Object]()
    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", list)
    map
  }
}
