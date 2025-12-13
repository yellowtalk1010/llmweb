package zuk.sast.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.tu_share.dto.TopInst
import zuk.tu_share.utils.TopInstUtil

import java.util
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(value=Array("top_inst"))
class TopInstController {

  private val log = LoggerFactory.getLogger(classOf[TopInstController])

  /**
   * 龙虎榜机构交易单
   * top_inst
   */
  @GetMapping(value=Array("list"))
  def all(search: String, tradedate: String): util.Map[String, Object] = {
    TopInstUtil.loadData()
    log.info(s"search: ${search}, tradedate: ${tradedate}")

    val list = new util.ArrayList[TopInst]()
    if(TopInstUtil.topInstMap.get(tradedate).nonEmpty){
      list.addAll(TopInstUtil.topInstMap.get(tradedate).get.asScala.filter(e=>{
        scala.collection.mutable.ListBuffer(e.ts_code,e.ts_name,e.hm_name,e.exalter).filter(s=>s!=null && s.contains(search)).size>0
      }).asJava)
    }

    val map = new util.HashMap[String, Object]()
    map.put("code", s"success")
    map.put("time", s"${System.currentTimeMillis()}")
    map.put("data", list)
    map
  }

}

