package zuk.sast.spring.controller

import com.alibaba.fastjson2.JSONWriter.Feature
import com.alibaba.fastjson2.{JSONArray, JSONObject}
import com.microsoft.playwright.{Page, Playwright, PlaywrightException}
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.spring.controller.component.{ApplicationProperties, TushareAllStocksCSVComponent, TushareConceptComponent, TushareInitMA4ModelMA5ModelComponent, TushareStockDailyDataComponent}
import zuk.sast.spring.controller.mapper.StockMapper
import zuk.sast.spring.controller.mapper.entity.StockEntity
import zuk.tu_share.dto.TsStock
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.utils.{HanLPUtil, TopInstUtil}

import java.io.{File, FileInputStream}
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util
import java.util.{Date, Properties, UUID}
import java.util.concurrent.Executors
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer
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

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null

  @Autowired
  val applicationProperties: ApplicationProperties = null

  @PostConstruct
  def init(): Unit = {
    val stockResultJsonPath = applicationProperties.getStockAnalysisSystem_resultJsonSavePath
    log.info(s"tushare推荐结果存储路径：${stockResultJsonPath}")
    if(!new File(stockResultJsonPath).exists()){
      log.error(s"tushare推荐结果存储路径：${stockResultJsonPath}。错误")
      System.exit(1)
    }

//    autoRefreshEasymoney()


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
    val stockResultJsonPath = applicationProperties.getStockAnalysisSystem_resultJsonSavePath
    val sdf = new SimpleDateFormat("yyyyMMdd")
//    val pro = System.getProperties
    log.info(s"stock result json path: ${stockResultJsonPath}")
    val file = new File(s"${stockResultJsonPath}${File.separator}${sdf.format(new Date())}")
    file
  }


  /***
   * 获取模型列表
   * @return
   */
  @GetMapping(value = Array("moduleList"))
  def moduleList(): util.Map[String, Object] = {

    val properties = new Properties()
    try {
      val propertiesPath = this.applicationProperties.getStockAnalysisSystemPath + File.separator + "stock_config.properties"
      properties.load(new FileInputStream(propertiesPath))
    }
    catch {
      case exception: Exception => exception.printStackTrace()
    }

    val list = PassFactory.moduleList().map(e=>{

      val map = util.HashMap[String, String]()
      val cls = e.getClass.getSimpleName
      //胜率
      val winRate = if(StringUtils.isEmpty(properties.getProperty(cls.toUpperCase)))
        ""
      else
        properties.getProperty(cls.toUpperCase)

      val name = e.desc()
      map.put("cls", cls)
      map.put("name", s"${cls}：${winRate}, ${e.desc()}")
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

    val stockResultJsonPath = applicationProperties.getStockAnalysisSystem_resultJsonSavePath

    val response = new util.HashMap[String, Object]()
    response.put("code", s"success")
    response.put("time", s"${System.currentTimeMillis()}")

    val file: File = getStockResultJsonPath()
    if(!file.exists() || !file.isDirectory){
      log.info(s"路径不存在: ${stockResultJsonPath}")
      return response
    }

    //获取选择的模型集合
    val modSet = if(PassFactory.moduleList().map(_.getClass.getSimpleName).contains(modType)) {
      Array(modType, "MA1_1_MODEL").toSet
    }
    else {
      //全部模型集合
      PassFactory.moduleList().map(_.getClass.getSimpleName).toSet
    }

    val simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
    val jsonfiles = file.listFiles().filter(_.getName.endsWith(".json"))

    var dateStr = simpleDateFormat.format(new Date())
    val filterJsonFiles = jsonfiles.filter(_.getName.startsWith(dateStr)).sortBy(_.getName).reverse
    log.info(s"json文件：${filterJsonFiles.map(_.getName).mkString("; ")}")

    val stockResultJsonList = filterJsonFiles.map(file=>{
      val fileJsonResultArray = JSONArray.parseArray(FileUtils.readFileToString(file, Charset.forName("UTF-8")), classOf[StockResultJson])
      fileJsonResultArray.asScala.map(e=>{
        val stockModleType = e.modClsName

        val tsStock = new TsStock(e.ts_code)
        e.eastmoneyURL = tsStock.eastmoneyURL
        e.conceptURL = tsStock.conceptURL

        e.file = file
        e.fileName = file.getName
        val optionTp3 = TushareStockDailyDataComponent.getIncreateRate(e.ts_code)

        val concept = this.tushareConceptComponent.getStockConceptInfo(e.ts_code)
        e.concept = concept
        e.remark = optionTp3.get._4 + concept

        if(Array(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR, TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR).contains(stockModleType.toUpperCase)){
          val closePrice = optionTp3.get._1
          if(closePrice >= 110.0){
            //如果当前价格大于200，不显示
//            Some(e)
            Option.empty
          }
          else{
            Some(e)
          }
        }
        else {
          Some(e)
        }
      }).filter(!_.isEmpty).map(_.get).sortBy(_.modWinRate).reverse
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
    //tushareInitMA4ModelMA5ModelComponent.add_MA4_MA5_MODEL(heads.toList ++ histories.toList)

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
    val stockModel4EntityList = tushareInitMA4ModelMA5ModelComponent.get_MODEL_LIST(TushareInitMA4ModelMA5ModelComponent.MA4_MODEL_STR)
    val ma4Set = pushStocks.filter(_._1.equals("MA4_MODEL")).flatMap(e=>e._2 ++ e._3).map(e=>{
      e.name = s"${e.name}【${stockModel4EntityList.filter(_.stockCode.trim.equals(e.ts_code.trim)).size}次】"
      e.ts_code
    }).toSet
    //非MA4_MODEL模块击中MA4股票
    pushStocks.filter(!_._1.equals("MA4_MODEL")).flatMap(e => e._2 ++ e._3).filter(e => ma4Set.contains(e.ts_code)).foreach(e => {
      e.name = s"${e.name}【击中MA4】"
    })

    val stockModel5EntityList = tushareInitMA4ModelMA5ModelComponent.get_MODEL_LIST(TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR)
    val ma5Set = pushStocks.filter(_._1.equals("MA5_MODEL")).flatMap(e => e._2 ++ e._3).map(e => {
      e.name = s"${e.name}【${stockModel5EntityList.filter(_.stockCode.trim.equals(e.ts_code.trim)).size}次】"
      e.ts_code
    }).toSet

    val stockModel7EntityList = tushareInitMA4ModelMA5ModelComponent.get_MODEL_LIST(TushareInitMA4ModelMA5ModelComponent.MA7_MODEL_STR)
    val ma7Set = pushStocks.filter(_._1.equals("MA7_MODEL")).flatMap(e => e._2 ++ e._3).map(e => {
      val size = stockModel7EntityList.filter(_.stockCode.trim.equals(e.ts_code.trim)).size
      e.name = if(size==0){
        s"${e.name}"
      }
      else{
        s"${e.name}【历史出现${size}次】"
      }
      e.ts_code
    }).toSet

    val allAttentionCodes = tushareStockController.getAllAttention() //全部关注的股票
    val allBuyCodes = tushareStockController.getAllBuy() //全部购买的股票
    val eliminateCodes = tushareStockController.getAllEliminate() //全部淘汰的股票



    val maplist = pushStocks.map(e=>{


      val conceptList = new ListBuffer[String]() //
      val fenci = HanLPUtil.createFenCi((e._2 ++ e._3).map(_.concept).toList)


      (e._2 ++ e._3).foreach(e=>{
        if(e.ts_code.startsWith("688")){
          e.name = e.name + "【科创】"
        }
        else if (e.ts_code.startsWith("920")) {
          e.name = s"${e.name}【北交所】"
        }

        //是否出现在龙虎榜中
        e.topInstitutions = TopInstUtil.existTopInst(e.ts_code)


        //
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
      map.put("time", s"${head.file.getName}-----${fenci}")
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

    response

  }


}
