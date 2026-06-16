package zuk.sast.controller

import com.alibaba.fastjson2.JSONWriter.Feature
import com.alibaba.fastjson2.{JSONArray, JSONObject}
import com.microsoft.playwright.{Page, Playwright, PlaywrightException}
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.controller.component.{TushareAllStocksCSVComponent, TushareInitMA4ModelMA5ModelComponent}
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.tu_share.dto.TsStock
import zuk.tu_share.pass.PassFactory

import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util
import java.util.{Date, UUID}
import java.util.concurrent.Executors
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

/***
 * 股票推荐列表
 */
@RestController
@RequestMapping(value = Array("push_stocks"))
@Component
class TusharePushStockController {

  private val log = LoggerFactory.getLogger(classOf[TusharePushStockController])

  private val Executor_Service = Executors.newCachedThreadPool()

  @Autowired
  private var tushareAllStocksCSVComponent: TushareAllStocksCSVComponent = null

  @Autowired
  private var tushareStockController: TushareStockController = null

  @Autowired
  private var tushareInitMA4ModelMA5ModelComponent: TushareInitMA4ModelMA5ModelComponent = _

  /***
   * 获取application.properties中的数据，股票json结果路径
   */
  @Value("${stock.result.json.path}")
  @BeanProperty
  private var stockResultJsonPath: String = null

  @PostConstruct
  def init(): Unit = {
    log.info(s"tushare推荐结果存储路径：${stockResultJsonPath}")
    if(!new File(stockResultJsonPath).exists()){
      log.error(s"tushare推荐结果存储路径：${stockResultJsonPath}。错误")
      System.exit(1)
    }

//    autoRefreshEasymoney()
//    tushareInitMA4ModelMA5ModelComponent.init_MODEL_BACK_TEST_RESULT()

  }

  /***
   * 自动刷新东方财富网址
   */
  private def autoRefreshEasymoney(): Unit = {

    Executor_Service.execute(()=>{

      val browser = Playwright.create.chromium.connectOverCDP("http://127.0.0.1:9222")
      log.info(s"启动自动刷新东方财富网页:${browser.contexts().size()}")
      while (true){
        try {
          browser.contexts().asScala.foreach(context=>{
            val pages = context.pages().asScala
            pages.filter(p => !p.isClosed && p.url().contains("eastmoney.com")).foreach(p=>{

              val url = p.url()
              val title = p.title()
              log.info(s"刷新东方财富网址名称[${title}]，url: ${url}")
              p.reload()
//              p.evaluate("location.reload()")
//              p.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED)
//              p.keyboard().press("F5")
//              p.reload(new ReloadOptions().setTimeout(5000))
              log.info(s"完成刷新东方财富网址名称[${title}]，url: ${url}")
              Thread.sleep(30 * 1000)
            })
          })
//          Thread.sleep(10 * 1000)
        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
            log.error(exception.getMessage)
          case _=>
        }
      }
    })
  }


  /***
   * 获取结果文件路径
   * @return
   */
  private def getStockResultJsonPath(): File = {
    val sdf = new SimpleDateFormat("yyyyMMdd")
//    val pro = System.getProperties
    log.info(s"stock result json path: ${this.stockResultJsonPath}")
    val file = new File(s"${this.stockResultJsonPath}${File.separator}${sdf.format(new Date())}")
    file
  }


  /***
   * 获取模型列表
   * @return
   */
  @GetMapping(value = Array("moduleList"))
  def moduleList(): util.Map[String, Object] = {

    var list = PassFactory.moduleList().map(e=>{
      val map = util.HashMap[String, String]()
      val cls = e.getClass.getSimpleName
      val name = e.desc()
      map.put("cls", cls)
      map.put("name", s"${cls}：${e.desc()}")
      map
    }).toBuffer

    val allMap = new util.HashMap[String, String]()
    allMap.put("cls", "ALL_MODEL")
    allMap.put("name", "全部模型")

    list.prepend(allMap)

    log.info(s"模型类型列表：${list.map(JSONObject.toJSONString(_)).mkString("; ")}")
    val map = new util.HashMap[String, Object]()
    map.put("code", "success")
    map.put("data", list.asJava)
    map
  }

  @GetMapping(value = Array("list"))
  def list(tradedate: String, modType: String): util.Map[String, Object] = {

    log.info(s"选择模型:${modType}")

    val modSet = if(PassFactory.moduleList().map(_.getClass.getSimpleName).contains(modType)) {
      Array(modType).toSet
    }
    else {
      PassFactory.moduleList().map(_.getClass.getSimpleName).toSet
    }

    val response = new util.HashMap[String, Object]()
    response.put("code", s"success")
    response.put("time", s"${System.currentTimeMillis()}")

    val simpleDateFormat = new SimpleDateFormat("yyyyMMdd")

    val file: File = getStockResultJsonPath()
    if(file.exists() && file.isDirectory){
      val jsonfiles = file.listFiles().filter(_.getName.endsWith(".json"))

      var dateStr = simpleDateFormat.format(new Date())
      val filterJsonFiles = jsonfiles.filter(_.getName.startsWith(dateStr)).sortBy(_.getName).reverse
      log.info(s"json文件：${filterJsonFiles.map(_.getName).mkString("; ")}")

      val stockResultJsonList = filterJsonFiles.map(file=>{
        val array = JSONArray.parseArray(FileUtils.readFileToString(file, Charset.forName("UTF-8")), classOf[StockResultJson])
        array.asScala.map(e=>{
          val tsStock = new TsStock()
          tsStock.ts_code = e.ts_code
          e.eastmoneyURL = tsStock.getEastmoneyURL()
          e.file=file
          e.fileName=file.getName
          e
        }).sortBy(_.modWinRate).reverse
      })

      val modWinRateClsNames = stockResultJsonList.flatMap(e=>e)
        .groupBy(_.modClsName)
        .filter(e=>modSet.map(_.toUpperCase).contains(e._1.toUpperCase))
        .map(e=>(e._1, e._2.toList.head)).toList
        .sortBy(_._2.modWinRate).reverse
        .map(e=>(e._1, e._2.modWinRate))

      log.info(s"胜率：${modWinRateClsNames.map(e=>{s"${e._1},${e._2}"}).mkString("; ")}")

      val heads = stockResultJsonList.head
      val histories = stockResultJsonList.slice(1, stockResultJsonList.length).flatMap(e=>e).sortBy(_.modWinRate).reverse

      //保存MA4_MODEL模型数据
      tushareInitMA4ModelMA5ModelComponent.add_MA4_MA5_MODEL(heads.toList ++ histories.toList)

      val pushStocks = modWinRateClsNames.map(_._1).map(clsName=>{
        //最新数据
        val headList = heads.filter(_.modClsName.equals(clsName)).sortBy(_.turnoverRate).reverse
        //历史数据
        val historyList = histories.filter(_.modClsName.equals(clsName)).groupBy(_.ts_code).map(e=>{
          if(e._2.size==1){
            e._2.head
          }
          else {
            e._2.sortBy(_.file.getName).head //取最早推荐
          }
        }).toList
          .filter(e=> !headList.map(_.ts_code).contains(e.ts_code)) //不能再headList中
          .sortBy(_.file.getName)
          .reverse
        (clsName, headList, historyList)
      })

      //获取MA4_MODEL分析到的股票代码
      val stockModel4EntityList = tushareInitMA4ModelMA5ModelComponent.get_MD4_MODEL_LIST()
      val ma4Set = pushStocks.filter(_._1.equals("MA4_MODEL")).flatMap(e=>e._2 ++ e._3).map(e=>{
        e.name = s"${e.name}【${stockModel4EntityList.filter(_.stockCode.trim.equals(e.ts_code.trim)).size}次】"
        e.ts_code
      }).toSet
      //非MA4_MODEL模块击中MA4股票
      pushStocks.filter(!_._1.equals("MA4_MODEL")).flatMap(e => e._2 ++ e._3).filter(e => ma4Set.contains(e.ts_code)).foreach(e => {
        e.name = s"${e.name}【击中MA4】"
      })

      val stockModel5EntityList = tushareInitMA4ModelMA5ModelComponent.get_MD5_MODEL_LIST()
      val ma5Set = pushStocks.filter(_._1.equals("MA5_MODEL")).flatMap(e => e._2 ++ e._3).map(e => {
        e.name = s"${e.name}【${stockModel5EntityList.filter(_.stockCode.trim.equals(e.ts_code.trim)).size}次】"
        e.ts_code
      }).toSet

      val allAttentionCodes = tushareStockController.getAllAttention() //全部关注的股票
      val allBuyCodes = tushareStockController.getAllBuy() //全部购买的股票
      val eliminateCodes = tushareStockController.getAllEliminate() //全部淘汰的股票

      val maplist = pushStocks.map(e=>{

        (e._2 ++ e._3).foreach(e=>{
          if(allAttentionCodes.contains(e.ts_code)){
            e.attention = "已关注"
          }
          if(allBuyCodes.contains(e.ts_code)){
            e.buy = "已购买"
          }
          if (eliminateCodes.contains(e.ts_code)) {
            e.eliminate = "已淘汰"
          }
        })

        val head = (e._2 ++ e._3).head
        val map = new util.HashMap[String, Object]()
        map.put("time", s"${head.file.getName}")
        map.put("module", s"【${head.modWinRate}】${head.modDesc}【${head.modClsName}】")
        map.put("heads", e._2.filter(e=> {
          !e.name.toUpperCase.contains("ST")
//            && e.turnoverRate.toFloat >= 0.3
        }).toList.asJava) //移除股票名称中带ST的股票
        map.put("histories", e._3.filter(e=> {
          !e.name.toUpperCase.contains("ST")
//            && e.turnoverRate.toFloat >= 0.3
        }).sortBy(_.turnoverRate).reverse.asJava) //移除股票名称中带ST的股票
        map
      }).asJava

      response.put("data", maplist)
    }
    else {
      log.info(s"路径不存在: ${this.stockResultJsonPath}")
    }

    response

  }


}
