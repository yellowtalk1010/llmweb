package zuk.tu_share.backtest

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.ModuleHistoryDailyData
import zuk.tu_share.module.*
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.send.ToBackTest
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, Dataset_module_history_rtk_dir}
import zuk.utils.SendMail

import java.io.{File, FileOutputStream}
import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import scala.beans.BeanProperty
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

case class BackTestDto() {
  @BeanProperty var stockCode: String = null
  @BeanProperty var stockName: String = null
  @BeanProperty var stockType: String = null
  @BeanProperty var tradedate: String = null

}

object BackTest {

  /***
   * 全量回测
   * 
   * @param map
   */
  def fullBacktest(map: mutable.HashMap[String, List[ModuleHistoryDailyData]]): Unit = {

    val fullBacktestModNameSet = Array(
      classOf[MA4_Model],
      classOf[MA5_Model],
      classOf[MA7_1_Model],
      classOf[MA8_Model],
      classOf[MA9_1_Model]
    ).map(_.getSimpleName.toUpperCase).toSet
    
    val funllBacktestFinishModules = ListBuffer[IModel]()
    val fullCount = new AtomicInteger(0)
    map.toList.foreach(tp2 => {
      val stockCode = tp2._1 //股票代码
      val stockName = tp2._2.head.name //股票名称

      //全量回测
      for (i <- 0 to ParseCammandParam.param.back_step) {
        val stockDataList = tp2._2.slice(i, tp2._2.size) //股票数据集
        println(s"全量回测${stockCode}, ${stockName}, ${stockDataList.last.trade_date} 至 ${stockDataList.head.trade_date}, 共${stockDataList.size}条, 进度= ${fullCount.incrementAndGet()}/${map.size * (ParseCammandParam.param.back_step + 1)}")

        val fullBacktestModules = PassFactory.engineModuleList().filter(m => fullBacktestModNameSet.map(_.toUpperCase).contains(m.getClass.getSimpleName.toUpperCase))
        PassFactory.doModule_1(fullBacktestModules, stockDataList)
        funllBacktestFinishModules ++= fullBacktestModules.filter(e => e.getStockDto() != null && e.getStockDto().tsStock != null)
      }
    })
    //保存全量回测分析的结果集
    BackTest.saveFullBacktestModuleResult(funllBacktestFinishModules.toList)
    
    this.analysis_winRate(fullBacktestModNameSet, map)
    
  }

  /***
   * 增量回测
   * 
   * @param map
   */
  def incrementBacktest(map: mutable.HashMap[String, List[ModuleHistoryDailyData]]): Unit = {

    val incrementBacktestModNameSet = Array(
      classOf[MA7_Model],
      classOf[MA9_Model]
    ).map(_.getSimpleName.toUpperCase).toSet
    
    val incrementBacktestFinishModules = ListBuffer[IModel]()
    val incrementCount = new AtomicInteger(0)
    map.toList.foreach(tp2 => {
      val stockCode = tp2._1 //股票代码
      val stockName = tp2._2.head.name //股票名称

      val LEN = 5 //增量分析最近10个交易日
      for (i <- 0 to LEN) {
        val stockDataList = tp2._2.slice(i, tp2._2.size) //股票数据集
        println(s"增量回测${stockCode}, ${stockName}, ${stockDataList.last.trade_date} 至 ${stockDataList.head.trade_date}, 共${stockDataList.size}条, 进度= ${incrementCount.incrementAndGet()}/${map.size * (LEN + 1)}")
 
        val incrementBacktestModules = PassFactory.engineModuleList().filter(m => incrementBacktestModNameSet.map(_.toUpperCase).contains(m.getClass.getSimpleName.toUpperCase))
        PassFactory.doModule_1(incrementBacktestModules, stockDataList)
        incrementBacktestFinishModules ++= incrementBacktestModules.filter(e => e.getStockDto() != null && e.getStockDto().tsStock != null)
      }
    })
    //保存增量回测分析的结果集
    BackTest.saveIncrementBacktestModuleResult(incrementBacktestFinishModules.toList)

    this.analysis_winRate(incrementBacktestModNameSet, map)
  }

  /**
   * 分析胜率
   *
   * @param map
   */
  private def analysis_winRate(backtestModNameSet: Set[String], map: mutable.HashMap[String, List[ModuleHistoryDailyData]]): Unit = {

    new File(ParseCammandParam.param.engineInfo.MODEL_BACK_TEST_RESULT_dir).listFiles().filter(f=>backtestModNameSet.contains(f.getName)).foreach(f=>{
      
      val clsName = f.getName
      
      val iModel = PassFactory.engineModuleList().filter(_.getClass.getSimpleName.toUpperCase.equals(clsName.toUpperCase)).head

      val lines = new ListBuffer[String]()
      val victoryStockList = ListBuffer[ModuleHistoryDailyData]()
      val count = new AtomicInteger(0)
      
      val jsonObjList = FileUtils.readLines(f, "UTF-8").asScala.map(l=> {
        val jsonObj = JSONObject.parseObject(l)
        jsonObj
      })
      
      jsonObjList.foreach(jsonObj=>{
        
        val stockType = jsonObj.get("stockType").asInstanceOf[String]
        val stockCode = jsonObj.get("stockCode").asInstanceOf[String]
        val stockName = jsonObj.get("stockName").asInstanceOf[String]
        val tradedate = jsonObj.get("tradedate").asInstanceOf[String]
        val status = if(jsonObj.get("status")==null){
          ""
        }
        else {
          jsonObj.get("status").asInstanceOf[String]
        }
        
        val dailyList = map.get(stockCode).get
        //todo 寻找买点
        val buy = dailyList.filter(_.trade_date.equals(tradedate)).head
        val buy_tsStock = Dataset_all_stocks_csv_file.getTsStock(buy.ts_code)
        val buyFutureList = dailyList.filter(_.trade_date.toLong > buy.trade_date.toLong).sortBy(_.trade_date.toLong) //倒序，从小到大
        //todo 寻找回测的卖点列表
        val sellList = if (buyFutureList.size >= iModel.backTestStep) {
          //如果未来长度大于模型设定的回测天数
          buyFutureList.take(iModel.backTestStep)
        }
        else {
          buyFutureList
        }
        
        if (sellList.size == 0) {
          val name = s"<a href='${buy_tsStock.eastmoneyURL}'>${buy.name}</a>"
          val line = s"${stockType}, ${buy.ts_code}, ${name}, ${buy.trade_date}【买入】, 【未交易】"
          lines += line
        }
        else {

          count.incrementAndGet()
          
          var sell: ModuleHistoryDailyData = null
          var sellFirstWinChangeStr = "失败"
          val step = new AtomicInteger(1) //从买入到卖出间隔多少个交易日，今日买次日卖，至少一个交易日
          sellList.foreach(e => {
            val winChange = new BigDecimal(e.high.toFloat - buy.close.toFloat).divide(new BigDecimal(buy.close), 4, RoundingMode.UP).multiply(new BigDecimal(100))
            if (winChange.compareTo(new BigDecimal(ParseCammandParam.param.wrate)) >= 0 
              && sell == null
            ) {
              sell = e
              sellFirstWinChangeStr = s"${sell.trade_date} 【${winChange}，卖出，${step.get()}个交易日】"
            }
            
            if(sell == null){
              step.incrementAndGet()
            }
            
          })

          if (sell != null) {
            //记录成功的
            victoryStockList += sell
          }

          val ok = if (sell != null) "" else "X"

          val name = s"<a href='${buy_tsStock.eastmoneyURL}'>${buy.name}</a>"
          val line = s"${stockType}, ${buy.ts_code}, ${name}, ${buy.trade_date}, 【买入】, ${sellFirstWinChangeStr}, ${ok}"
          lines += line

        }
        
      })
      
      //计算胜率
      val victoryRate = new BigDecimal(victoryStockList.size).divide(new BigDecimal(count.get()), 4, RoundingMode.UP)
      //胜率保存到properties中
      Dataset_module_history_rtk_dir.getProperties().put(clsName.toUpperCase, victoryRate.toString)
      val line = s"${clsName}胜率：${victoryRate}, ${iModel.desc()}"
      lines += line
      println(line)

      Dataset_module_history_rtk_dir.storeProperties()

      sendMail(clsName, lines.mkString("<br>\n"))
      
    })
    
  }

  /**
   * 保存增量分析的结果集
   */
  private def saveIncrementBacktestModuleResult(iModelList: List[IModel]): Unit = {
    iModelList.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null)
      .groupBy(_.getClass.getSimpleName)
      .foreach(tp2=>{
        val clsName = tp2._1.toUpperCase
        val ls = tp2._2

        val mapList = ls.map(iMod=>{
          val jsonObj = new JSONObject()
          jsonObj.put("stockType", clsName) //模型类型
          jsonObj.put("stockCode", iMod.getStockDto().tsStock.ts_code) //股票代码
          jsonObj.put("stockName", iMod.getStockDto().tsStock.name) //股票名称
          jsonObj.put("tradedate", iMod.getStockDto().tradedate) //交易日期
          jsonObj.put("status", "") //状态

          val key = s"${jsonObj.get("tradedate")}-${jsonObj.get("stockCode")}"
          (key, jsonObj)
        })

        val btResFile = new File(ParseCammandParam.param.engineInfo.MODEL_BACK_TEST_RESULT_dir + File.separator + clsName)
        println(s"读取${clsName}结果文件，路径：${btResFile.getAbsolutePath}，${btResFile.exists()}")
        val historyMap = FileUtils.readLines(btResFile, "UTF-8").asScala.map(l=>{
          val jsonObj = JSONObject.parseObject(l)
          val key = s"${jsonObj.get("tradedate")}-${jsonObj.get("stockCode")}"
          (key, jsonObj)
        })
        val historyMapKeySet = historyMap.map(_._1)

        mapList.filter(e=> !historyMapKeySet.contains(e._1)).foreach(e=>{
          historyMap += e
        })

        val btList = historyMap.toList.sortBy(_._1).reverse.map(_._2).map(jsonObj=>{
          val e = JSONObject.toJSONString(jsonObj, Feature.LargeObject, Feature.MapSortField)
          e
        }).asJava

        FileUtils.writeLines(btResFile, btList)
        println(s"${clsName}模型回测结果重新写入路径:${btResFile.getAbsolutePath}")

      })
  }

  /** *
   * 保存全量分析的结果集
   */
  private def saveFullBacktestModuleResult(iModelList: List[IModel]): Unit = {

    iModelList.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null)
      .groupBy(_.getClass.getSimpleName)
      .foreach(tp2=>{
        val clsName = tp2._1.toUpperCase
        val ls = tp2._2

        val btList = ls.map(iMod=>{
            val map = new util.HashMap[String, String]()
            map.put("stockType", clsName) //模型类型
            map.put("stockCode", iMod.getStockDto().tsStock.ts_code) //股票代码
            map.put("stockName", iMod.getStockDto().tsStock.name) //股票名称
            map.put("tradedate", iMod.getStockDto().tradedate) //交易日期
            map.put("status", "") //状态
            map
          }).sortBy(m=>{
            s"${m.get("tradedate")}-${m.get("stockCode")}"
          })
          .reverse
          .map(m => {
            val l = JSONObject.toJSONString(m, Feature.LargeObject)
            l
          }).asJava

        val btResPath = ParseCammandParam.param.engineInfo.MODEL_BACK_TEST_RESULT_dir + File.separator + clsName
        FileUtils.writeLines(new File(btResPath), btList)
        println(s"${clsName}模型回测结果写入路径:${btResPath}")

    })
  }

  private def sendMail(clsName: String, htmlContent: String) = {
    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"【${clsName}】${tradeDate}【backtest】【${ParseCammandParam.param.wrate} 】", htmlContent)
  }

}
