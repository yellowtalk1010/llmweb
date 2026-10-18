package zuk.sast.spring.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

import java.util
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

private case class PageDto(@BeanProperty id: String, 
                           @BeanProperty title: String, 
                           @BeanProperty url: String
                          )

/***
 * 获取收藏的页面
 */
@RestController
@RequestMapping(value = Array("pages"))
@Component
class TusharePagesController {

  private val log = LoggerFactory.getLogger(classOf[TusharePagesController])
  
  @GetMapping(value = Array("list"))
  def getList(): util.Map[String, Object] = {
    
    val list = ListBuffer[PageDto]()

    list += PageDto("0", "sast", "http://localhost:8080/config")
    list += PageDto("1", "H2", "http://localhost:1010/frame.jsp?jsessionid=31f6e75368954a14")
    list += PageDto("2", "stockapi地址", "https://stockapi.com.cn/")
    list += PageDto("3", "招商地址", "https://xtrade.newone.com.cn/ssologin?t=jykstd")
    list += PageDto("4", "大盘云图", "https://www.dapanyuntu.com/")
    list += PageDto("5", "tushare", "https://tushare.pro/document/2")
    
    list += PageDto("111", "龙虎榜", "TopInst")
    list += PageDto("112", "我的关注", "AllStocks")
    list += PageDto("113", "股票推荐", "PushTushareStock")

    val result = new util.HashMap[String, Object]()
    result.put("data", list.asJava)
    result.put("code", "success")
    result
  }

}
