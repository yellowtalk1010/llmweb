package zuk.sast.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.util

/***
 *
 * 使用scala开发controller接口
 *
 * 访问： http://localhost:8080/testStock/list
 */
@RestController
@RequestMapping(value=Array("hlStock"))
class HlStockController {

  @GetMapping(value=Array("list"))
  def all(search: String): util.Map[String, String] = {
    val map = new util.HashMap[String, String]()
    map.put("code", "success")
    map.put("time", s"${System.currentTimeMillis()}")
    map
  }

}
