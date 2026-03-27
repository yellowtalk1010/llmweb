package zuk.sast.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.tu_share.dto.TopInst
import zuk.tu_share.utils.TopInstUtil

import java.util
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

import org.springframework.beans.factory.annotation.{Autowired, Value}

@RestController
@RequestMapping(value=Array("top_inst"))
class TopInstController {

  private val log = LoggerFactory.getLogger(classOf[TopInstController])

  /** *
   * 获取application.properties中的数据，股票json结果路径
   */
  @Value("${stock.hm.top_inst.path}")
  @BeanProperty
  private var stockHmTopInstPath: String = null

  /**
   * 龙虎榜机构交易单
   * top_inst
   */
  @GetMapping(value=Array("list"))
  def all(search: String, tradedate: String): util.Map[String, Object] = {
    TopInstUtil.loadData(stockHmTopInstPath)
    log.info(s"search: ${search}, tradedate: ${tradedate}")

    val list = new util.ArrayList[TopInst]()
    if(TopInstUtil.loadData(stockHmTopInstPath).get(tradedate).nonEmpty){
      list.addAll(TopInstUtil.loadData(stockHmTopInstPath).get(tradedate).get.asScala.filter(e=>{
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

