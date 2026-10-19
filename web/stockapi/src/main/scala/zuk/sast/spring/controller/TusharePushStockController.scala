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
import zuk.sast.spring.controller.component.{TushareConceptComponent, TushareInitMA4ModelMA5ModelComponent}
import zuk.sast.spring.controller.mapper.StockMapper
import zuk.sast.spring.controller.mapper.entity.StockEntity
import zuk.sast.spring.controller.service.tushareStockImpl.{QuerTushareStockDto, TushareStock_limit_down_Impl, TushareStock_limit_up_Impl, TushareStock_module_Impl}
import zuk.similar.SimilartyUtil
import zuk.tu_share.{DataFrame, ParseCammandParam}
import zuk.tu_share.dto.TsStock
import zuk.tu_share.module.{IModel, MA8_Model, MA9_Model}
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.utils.{Dataset_top_Inst_dir, FenCi_Util, IncreateDecreateRateDescUtil}

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
  private var tushareInitMA4ModelMA5ModelComponent: TushareInitMA4ModelMA5ModelComponent = _

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null

  //获取跌停
  @Autowired
  private var tushareStock_limit_down_Impl: TushareStock_limit_down_Impl = null

  //获取涨停
  @Autowired
  private var tushareStock_limit_up_Impl: TushareStock_limit_up_Impl = null

  @PostConstruct
  def init(): Unit = {
    val stockResultJsonPath = ParseCammandParam.param.engineInfo.result_json_dir
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
  private def getStockResultJsonPath(tradeDate: String = ""): File = {


    if(StringUtils.isNotBlank(tradeDate)){
      val file = new File(s"${ParseCammandParam.param.engineInfo.result_json_dir}${File.separator}${tradeDate}")
      file
    }
    else {
      val sdf = new SimpleDateFormat("yyyyMMdd")
      val file = new File(s"${ParseCammandParam.param.engineInfo.result_json_dir}${File.separator}${sdf.format(new Date())}")
      file
    }

  }


  /***
   * 根据模型的胜率排序
   * @return
   */
  private def getModuleSortList: List[IModel] = {
    try {
      val properties = DataFrame.getProperties()
      PassFactory.engineModuleList().sortBy(_.winRate).reverse
    }
    catch {
      case exception: Exception =>
        exception.printStackTrace()
        List.empty
    }
  }


  /***
   * 获取模型列表
   * @return
   */
  @GetMapping(value = Array("moduleList"))
  def moduleList(): util.Map[String, Object] = {
    
    val sortedModels = getModuleSortList
    val list = sortedModels.map(e=>{

      val map = util.HashMap[String, String]()
      val cls = e.getClass.getSimpleName

      val name = e.desc()
      map.put("cls", cls)
      map.put("name", s"${cls}：${e.winRate} ${e.desc()}") //输出模型名称，胜率，模型描述
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

  /** *
   * 获取关注股票
   *
   * @return
   */
  def getAllAttention(): Set[String] = {
    TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.attention_str)).map(_.stockCode).toSet
  }

  /** *
   * 获取购买股票
   *
   * @return
   */
  def getAllBuy(): Set[String] = {
    TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.buy_str)).map(_.stockCode).toSet
  }

  /***
   * 输出模型推荐的结果
   * 
   * @param tradedate 交易日期
   * @param modType 选择的模型名称
   * @return
   */
  @GetMapping(value = Array("list"))
  def list(tradedate: String, modType: String, group: Boolean): util.Map[String, Object] = {


    val response = new util.HashMap[String, Object]()
    response.put("code", s"success")
    response.put("time", s"${System.currentTimeMillis()}")

    val trade_date = if(StringUtils.isNotBlank(tradedate))
      tradedate.replaceAll("-", "")
    else {
      val sdf = new SimpleDateFormat("yyyyMMdd")
      sdf.format(new Date())
    }
    
    log.info(s"选择模型:${modType}， 交易日期：${trade_date}")

    //TODO 从涨停和跌停中获取涨停的板块
    val hotConcepts = this.tushareStock_limit_up_Impl.getStocks(QuerTushareStockDto("", TushareStock_limit_up_Impl.limit_up, trade_date, trade_date)).asScala.map(_.concept).toList
    val hotFenCiList = FenCi_Util.createFenCi(hotConcepts)._1.take(50)
    val coolConcepts = this.tushareStock_limit_down_Impl.getStocks(QuerTushareStockDto("", TushareStock_limit_down_Impl.limit_down, trade_date, trade_date)).asScala.map(_.concept).toList
    val coolFenCiList = FenCi_Util.createFenCi(coolConcepts)._1.take(50)

    //判断结果路径是否存在
    val file: File = new File(s"${ParseCammandParam.param.engineInfo.result_json_dir}${File.separator}${trade_date}")
    log.info(s"推荐结构路径:${file.getAbsolutePath}, ${file.exists()}")
    if(!file.exists()){
      response.put("data", Array(new util.HashMap[String, Object]()).toList.asJava)
      log.info(s"分析引擎结果路径不存在: ${file.getAbsolutePath}")
      return response
    }

    //获取选择的模型集合
    val modSet = (if(PassFactory.engineModuleList().map(_.getClass.getSimpleName).contains(modType)) {
      Array(modType, classOf[MA9_Model].getSimpleName.toUpperCase).toSet  //默认输出 MA9 模型
    }
    else {
      //全部模型集合
      PassFactory.engineModuleList().map(_.getClass.getSimpleName).toSet
    })
      .filter(_.equals(classOf[MA8_Model].getSimpleName.toUpperCase))  //移除MA8，不想粘龙虎榜（思考1%收益和-5%亏损的胜率和赔率）

    /***
     * 列出今天模型推荐的全部结果文件集
     */
    val jsonfiles = file.listFiles().filter(_.getName.endsWith(".json"))

    val todayModelAdviseJsonFiles = jsonfiles.filter(_.getName.startsWith(trade_date)).sortBy(_.getName).reverse
    log.info(s"输出今日模型推荐的全部结果json文件：${todayModelAdviseJsonFiles.map(_.getName).mkString("; ")}")

    //TODO 从推荐结果文件中读取结果对象
    val stockResultJsonList = todayModelAdviseJsonFiles.map(file=>{
      val fileJsonResultArray = JSONArray.parseArray(FileUtils.readFileToString(file, Charset.forName("UTF-8")), classOf[StockResultJson])
      fileJsonResultArray.asScala.map(e=>{
        e.similarity = e.similartyDesc //写入引擎计算的相似度数据
        val stockModleType = e.modClsName

        val tsStock = new TsStock(e.ts_code)
        e.eastmoneyURL = tsStock.eastmoneyURL
        e.conceptURL = tsStock.conceptURL
        e.zjlxURL = tsStock.zjlxURL

        e.file = file
        e.fileName = file.getName
        val optionTp3 = IncreateDecreateRateDescUtil.getDescription(e.ts_code)

        val concept = this.tushareConceptComponent.getStockConceptInfo(e.ts_code)
        e.concept = concept
        e.remark = optionTp3.get._4 + concept
        e.tradeDate = trade_date
        e
      }).sortBy(_.modWinRate).reverse
    })

    //
    val modWinRateClsNames = stockResultJsonList.flatMap(e=>e)
      .groupBy(_.modClsName)
      .filter(e=>modSet.map(_.toUpperCase).contains(e._1.toUpperCase))
      .map(e=>(e._1, e._2.toList.head)).toList
      .sortBy(_._2.modWinRate).reverse //根据胜率排序
      .map(e=>(e._1, e._2.modWinRate))

    log.info(s"模型胜率从高至低排序：${modWinRateClsNames.map(e=>{s"${e._1},${e._2}"}).mkString("; ")}")

    //获取最新的推荐数据
    val heads = stockResultJsonList.head
    //获取历史的推荐数据
    val histories = stockResultJsonList.slice(1, stockResultJsonList.length).flatMap(e=>e).sortBy(_.modWinRate).reverse

    //TODO 过滤掉历史数据中重复出现的数据，仅保留最近的历史数据。并且历史数据不能在 最新的 heads 数据中
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
        .filter(e=> !headList.map(_.ts_code).contains(e.ts_code)) //不能在headList中
        .sortBy(_.file.getName)
        .reverse
      (clsName, headList, historyList)
    })

    //TODO 计算历史上出现的次数

    pushStocks.flatMap(e=>e._2 ++ e._3).foreach(e=>{

      if (e.ts_code.startsWith("688")) {
        e.name = e.name + "【科创】"
      }
      else if (e.ts_code.startsWith("920")) {
        e.name = s"${e.name}【北交所】"
      }

      val historyTimes = TushareStock_module_Impl.modelTestbackResultMap.asScala.map(tp2 => {
        val times = tp2._2.filter(_.stockCode.equals(e.ts_code))
          .filter(_.tradedate.toLong < e.tradeDate.toLong)
          .size
        
        if(times==0){
          ""
        }
        else {
          s"${tp2._1}(${times}次)"
        }
      }).filter(StringUtils.isNotBlank(_)).mkString(";").replaceAll("_MODEL", "")
      e.name = s"${e.name}【${historyTimes}】"
    })


    val allAttentionCodes = getAllAttention() //全部关注的股票
    val allBuyCodes = getAllBuy() //全部购买的股票
    
    val maplist = pushStocks
      .map(e=>(e._1,
        e._2.sortBy(_.historyHitCount).reverse.filter(! _.name.contains("ST")),
        e._3.sortBy(_.historyHitCount).reverse.filter(! _.name.contains("ST")))
      )
      .filter(e=>(e._2 ++ e._3).size > 0)
      .map(e=>{

        val conceptList = new ListBuffer[String]() //
        val fenci = FenCi_Util.createFenCi((e._2 ++ e._3).map(_.concept).toList)._2

        (e._2 ++ e._3).foreach(e=>{

          val fenciSet = FenCi_Util.createFenCi(List(e.concept))._1.map(_._1).toSet
          //todo 关联热点板块
          e.hotSectors = fenciSet.flatMap(w=>{
            hotFenCiList.filter(hw=>hw._1.equals(w))
          }).toList
            .sortBy(_._2).reverse
            .map(e=>{
              s"${e._1}【${e._2}/${hotConcepts.size}】"
            }).mkString("; ")
          //todo 关联冰点板块
          e.coolSectors = fenciSet.flatMap(w=>{
            coolFenCiList.filter(hw=>hw._1.equals(w))
          }).toList
            .sortBy(_._2).reverse
            .map(e=>{
              s"${e._1}【${e._2}/${coolConcepts.size}】"
            }).mkString("; ")

          //是否出现在龙虎榜中
          e.topInstitutions = Dataset_top_Inst_dir.existTopInst(e.ts_code)

          //
          if(allAttentionCodes.contains(e.ts_code)){
            e.attention = "已关注"
          }
          if(allBuyCodes.contains(e.ts_code)){
            e.buy = "已购买"
          }
        })

        e._2.zipWithIndex.foreach(tp2=>{
          if(group){
            tp2._1.similarity = SimilartyUtil.getTsCode(tp2._1.ts_code, trade_date).desc //相似度计算
            println(s"${tp2._1.modClsName}=================相似度计算完成: ${tp2._2 + 1}/ ${e._2.size}")
          }
        })

        val head = (e._2 ++ e._3).head
        val map = new util.HashMap[String, Object]()
        map.put("time", s"${head.file.getName}-----${fenci}")
        map.put("module", s"【${head.modWinRate}】${head.modDesc}【${head.modClsName}】")
        map.put("heads", e._2.asJava)
        map.put("histories", e._3.asJava)
        map
    }).asJava


    if(maplist.size() == 0){
      val emptyMap = new util.HashMap[String, Object]()
      emptyMap.put("time", "无")
      response.put("data", Array(emptyMap).toList.asJava)
    }
    else {
      response.put("data", maplist)
    }

    response

  }


}
