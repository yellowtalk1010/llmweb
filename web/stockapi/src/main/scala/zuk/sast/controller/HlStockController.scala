package zuk.sast.controller

import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.util

/***
 *
 * 使用scala开发controller接口
 *
 * 访问： http://localhost:8080/hlStock/list?search=xxxx
 *
 */
@Slf4j
@RestController
@RequestMapping(value=Array("hlStock"))
class HlStockController {

  private val log = LoggerFactory.getLogger(classOf[HlStockController])  // 与（@Slf4j）功能一样

  @GetMapping(value=Array("list"))
  def all(search: String): util.Map[String, String] = {

    log.info(s"search: ${search}")
    println(s"search: ${search}")

    val map = new util.HashMap[String, String]()
    map.put("code", s"success -> ${search}")
    map.put("time", s"${System.currentTimeMillis()}")
    map
  }

}
