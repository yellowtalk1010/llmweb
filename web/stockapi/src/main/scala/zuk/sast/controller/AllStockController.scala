package zuk.sast.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.controller.mapper.ZukStockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.stockapi.{LoaderLocalStockData, StockApiVo}

import java.util
import java.util.stream.Collectors
import java.util.{Arrays, HashMap, List, Map, Set, UUID}

@RestController
@RequestMapping(value = Array("stock"))
class AllStockController {

  private val log = LoggerFactory.getLogger(classOf[AllStockController])

  @Autowired private var zukStockMapper: ZukStockMapper = _

  @GetMapping(value = Array("delete"))
  def delete(api_code: String): util.Map[String, Object] = {
    log.info("delete:" + api_code)
    val result: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
    try {
      this.zukStockMapper.deleteByCode(api_code)
      result.put("status", "ok")
      return result
    } catch {
      case e: Exception =>
        //e.printStackTrace();
        log.error(e.getMessage)
        result.put("status", e.getMessage)
    }
    result
  }

  @GetMapping(value = Array("add"))
  def add(api_code: String): util.Map[String, Object] = {
    log.info("add:" + api_code)
    val result = new util.HashMap[String, Object]
    try {
      val list = this.zukStockMapper.selectByCode(api_code)
      if (list != null && list.size > 0) {
        result.put("status", "已存在")
        return result
      }
      else {
        LoaderLocalStockData.STOCKS.stream.filter((stock: StockApiVo) => stock.getApi_code == api_code).forEach((socket: StockApiVo) => {
          val stockEntity = new StockEntity
          stockEntity.id = UUID.randomUUID.toString
          stockEntity.code = socket.getApi_code
          stockEntity.jys = socket.getJys
          stockEntity.name = socket.getName
          this.zukStockMapper.insert(stockEntity)

        })
        result.put("status", "ok")
        return result
      }
    } catch {
      case e: Exception =>
        //e.printStackTrace();
        log.error(e.getMessage)
        result.put("status", e.getMessage)
    }
    result
  }

  /** *
   * 我的关注
   *
   * @return
   */
  @GetMapping(value = Array("my")) def my: util.Map[String, Object] = {
    try {
      val stockEntities = zukStockMapper.selectAll()
      val map = new util.HashMap[String, AnyRef]
      val sets = stockEntities.stream.map((e: StockEntity) => e.code).collect(Collectors.toSet)
      val ls = LoaderLocalStockData.STOCKS.stream.filter((stock: StockApiVo) => {
        sets.contains(stock.getApi_code)

      }).toList
      map.put("stocks", ls)
      return map
    } catch {
      case e: Exception =>
        e.printStackTrace()
        log.error(e.getMessage)
    }
    new util.HashMap[String, Object]
  }

  val tags: util.Set[String] = util.Arrays.asList(
    "人工智能",
    "deepseek",
    "昇腾",
    "数据中心",
    "芯片",
    "华为",
    "金融",
    "消费",
    "AI",
    "稀土",
    "工业母机").stream.map((e: String) => e.toUpperCase).collect(Collectors.toSet)

  @GetMapping(value = Array("all"))
  def all(search: String): Map[String, Object] = {
    var list: List[StockApiVo] = null
    if (search != null && search.length > 0) {
      val splits: Set[String] = Arrays.stream(search.split("\n")).filter((e: String) => e != null && e.trim.length > 0).map((e: String) => e.toUpperCase).collect(Collectors.toSet)
      list = LoaderLocalStockData.STOCKS.stream.filter((e: StockApiVo) => {
        splits.stream.filter((s: String) => {
          e.getGl.toUpperCase.contains(s) || e.getApi_code.toUpperCase.contains(s) || e.getName.toUpperCase.contains(s)

        }).count > 0

      }).collect(Collectors.toList)
    }
    else {
      //没有输入条件，则默认输出1000条
      list = LoaderLocalStockData.STOCKS.stream.filter((e: StockApiVo) => {
        tags.stream.filter((tag: String) => {
          e.getGl.toUpperCase.contains(tag)

        }).count > 0

      }).collect(Collectors.toList)
      if (list != null && list.size > 1000) {
        list = list.subList(0, 1000)
      }
    }
    log.info("search:" + search + ", stocks:" + list.size + ", blocks:" + tags.size)
    val map: Map[String, AnyRef] = new HashMap[String, AnyRef]
    map.put("stocks", list)
    map.put("blocks", tags)
    map
  }

}
