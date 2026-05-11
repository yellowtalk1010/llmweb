package zuk.sast.controller

import com.alibaba.fastjson2.JSONWriter.Feature
import com.alibaba.fastjson2.{JSONArray, JSONObject}
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.controller.component.TushareAllStocksCSVComponent
import zuk.tu_share.dto.TsStock

import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.concurrent.Executors
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

/***
 * 股票推荐列表
 */
@RestController
@RequestMapping(value = Array("attention"))
@Component
class TushareAttentionStockController {

  private val log = LoggerFactory.getLogger(classOf[TushareAttentionStockController])

  @Autowired
  private var tushareAllStocksCSVComponent: TushareAllStocksCSVComponent = null

  /** *
   * 获取application.properties中的数据，股票json结果路径
   */
  @Value("${stock.result.json.path}")
  @BeanProperty
  private var stockResultJsonPath: String = null

  /***
   * 获取关注股票
   * @return
   */
  def getAllAttention(): Tuple2[File, Set[String]] = {
    val file = new File(s"${this.stockResultJsonPath}${File.separator}attention.jsons")
    log.info(s"关注数据文件路径:${file.getAbsolutePath}")
    if (!file.exists()) {
      log.error(s"${file.getAbsolutePath}文件不存在")
      file.mkdirs()
      file.createNewFile()
    }
    val sets = FileUtils.readLines(file, Charset.forName("UTF-8")).asScala.toSet
    (file, sets)
  }

  /***
   * 索取全部关注
   */
  @GetMapping(value = Array("all"))
  def all(desc: String): util.Map[String, Object] = synchronized {
    log.info(s"全部关注:${desc}")
    val all = getAllAttention()

    val file = all._1
    val set = all._2

    val tsStockList = set.toList.map(e=>{
      tushareAllStocksCSVComponent.getTsStock(e)
    }).filter(!_.isEmpty).asJava

    val result = new util.HashMap[String, Object]()
    result.put("data", tsStockList)
    result
  }

  /***
   * 移除关注
   */
  @GetMapping(value = Array("delete"))
  def delete(tsCode: String): util.Map[String, String] = synchronized {
    log.info(s"删除关注:${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")
    val all = getAllAttention()

    val file = all._1
    val set = all._2

    val result = new util.HashMap[String, String]()
    result.put("code", "success")

    val list = set.filter(! _.equals(tsCode)).toList
    FileUtils.writeLines(file, list.asJava)

    result
  }

  /***
   * 添加关注
   */
  @GetMapping(value = Array("add"))
  def add(tsCode: String): util.Map[String, String] = synchronized {
    log.info(s"添加关注:${tsCode}, ${tushareAllStocksCSVComponent.getTsStock(tsCode).getOrElse(new TsStock).name}")
    val all = getAllAttention()
    val file = all._1
    val set = all._2

    val result = new util.HashMap[String, String]()
    result.put("code", "success")
    if(set.contains(tsCode)){
      result.put("desc", s"已存在，${tsCode}")
    }
    else {
      val list = set.toBuffer
      list += tsCode
      FileUtils.writeLines(file, list.asJava)
      result.put("desc", s"添加成功，${tsCode}")
    }
    log.info(JSONObject.toJSONString(result))
    result
  }
}
