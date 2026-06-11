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
import zuk.sast.controller.component.TushareAllStocksCSVComponent
import zuk.sast.controller.mapper.StockMapper
import zuk.sast.controller.mapper.entity.StockEntity
import zuk.tu_share.dto.TsStock

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
  private var stockMapper: StockMapper = _

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

    refreshEasymoney()

  }

  /***
   * 初始化历史 MA4_MODEL 模型生成的历史数据
   */
  private init_MA4_MODEL_HISTORY(): Unit = {
//    val file = new File("D:\\development\\github\\llmweb\\web\\MA4_MODEL.txt")
//    val lines = FileUtils.readLines(file, "UTF-8")
//    lines.asScala.foreach(line=>{
//      val arr = line.split(",").toList
//      val stockType = arr(0)
//      val stockCode = arr(1)
//      val name = arr(2)
//      val time = arr(4).replaceAll("【买入】", "") + "101010"
//      println(s"${stockType}, ${stockCode}, ${name}, ${time}")
//
//      val stock = new StockEntity
//      stock.id = UUID.randomUUID().toString.replaceAll("-", "")
//      stock.stockCode = stockCode
//      stock.name = name
//      stock.stockType = "MA4_MODEL"
//      stock.createtime = time
//      stockMapper.insert(stock)
//    })
  }


  /***
   * 刷新东方财富网址
   */
  private def refreshEasymoney(): Unit = {

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
              Thread.sleep(3 * 1000)
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

  @GetMapping(value = Array("list"))
  def list(tradedate: String): util.Map[String, Object] = {

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

      val modWinRateClsNames = stockResultJsonList.flatMap(e=>e).groupBy(_.modClsName).map(e=>(e._1, e._2.toList.head)).toList.sortBy(_._2.modWinRate).reverse.map(e=>(e._1, e._2.modWinRate))
      log.info(s"胜率：${modWinRateClsNames.map(e=>{s"${e._1},${e._2}"}).mkString("; ")}")

      val heads = stockResultJsonList.head
      val histories = stockResultJsonList.slice(1, stockResultJsonList.length).flatMap(e=>e).sortBy(_.modWinRate).reverse

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
      val ma4Set = pushStocks.filter(_._1.equals("MA4_MODEL")).flatMap(e=>e._2 ++ e._3).map(_.ts_code).toSet
      //非MA4_MODEL模块击中MA4股票
      pushStocks.filter(!_._1.equals("MA4_MODEL")).flatMap(e=>e._2 ++ e._3).filter(e=>ma4Set.contains(e.ts_code)).foreach(e=>{
        e.name = s"${e.name}【击中MA4】"
      })


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
          !e.name.toUpperCase.contains("ST") && e.turnoverRate.toFloat >= 0.3
        }).toList.asJava) //移除股票名称中带ST的股票
        map.put("histories", e._3.filter(e=> {
          !e.name.toUpperCase.contains("ST") && e.turnoverRate.toFloat >= 0.3
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
