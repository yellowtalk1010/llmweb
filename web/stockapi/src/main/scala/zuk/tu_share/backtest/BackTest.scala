package zuk.tu_share.backtest

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.ModuleHistoryDailyData
import zuk.tu_share.module.{IModel, StockDto}
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.send.ToBackTest
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, Dataset_module_history_rtk_dir}
import zuk.utils.SendMail

import java.io.{File, FileOutputStream}
import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util
import java.util.Date
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
  def analysis_full(map: mutable.HashMap[String, List[ModuleHistoryDailyData]]): Unit = {
    
    val linesMap = new util.HashMap[String, ListBuffer[String]]()
    val backTestMapList = new ListBuffer[util.HashMap[String, String]]
    
    
    val memoryList = ToBackTest.memoryBackTestList
    val modGroupMap = memoryList.groupBy(_.getClass.getSimpleName)
    modGroupMap.toList.foreach(tp2=>{
      
      val modClsName = tp2._1 //模型类名
      val iModel = PassFactory.engineModuleList().filter(_.getClass.getSimpleName.equals(modClsName)).head

      val lines = new ListBuffer[String]()
      
      //模型筛选的股票按时间排序
      val modList = tp2._2.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null).sortBy(e=>(e.getStockDto().tradedate, e.getStockDto().tsStock.ts_code)).reverse
      
      //模型筛选的股票回测成功的股票集
      val victoryStockList = ListBuffer[StockDto]()
      
      modList.foreach(iMod=>{

        val chooseStockDto = iMod.getStockDto()

        if(!map.get(chooseStockDto.tsStock.ts_code).isEmpty){
          val dailyList = map.get(chooseStockDto.tsStock.ts_code).get
          
          //todo 构建买点
          val buy = dailyList.filter(_.trade_date.equals(chooseStockDto.tradedate)).head
          
          val buyFutureList = dailyList.filter(_.trade_date.toLong > chooseStockDto.tradedate.toLong).sortBy(_.trade_date.toLong) //倒序，从小到大
          
          //todo 构建卖点
          val sellList = if(buyFutureList.size >= iModel.backTestStep){
            //如果未来长度大于模型设定的回测天数
            buyFutureList.take(iModel.backTestStep)
          }
          else {
            buyFutureList
          }
          
          if(sellList.size==0){
            
            val name = s"<a href='${chooseStockDto.tsStock.eastmoneyURL}'>${chooseStockDto.tsStock.name}</a>"
            val buyReason = s"<span>买入</span>"

            val line = s"${modClsName}, ${chooseStockDto.tsStock.ts_code}, ${name},【${chooseStockDto.totalMV}亿，${chooseStockDto.limitUp}，${chooseStockDto.limitDown}，${chooseStockDto.turnoverRate}，涨跌${chooseStockDto.preChangeRate}】, ${chooseStockDto.tradedate}【${buyReason}】, 【未交易】"
            lines += line

            val map = new util.HashMap[String, String]()
            map.put("stockType", modClsName.toUpperCase.trim)
            map.put("stockCode", buy.ts_code.trim)
            map.put("stockName", buy.name.trim)
            map.put("tradedate", buy.trade_date.trim)
            backTestMapList += map
            
          }
          else {
            
            var st = false
            
            val highStr = sellList.map(e => {
              val change = new BigDecimal(e.high.toFloat - buy.close.toFloat).divide(new BigDecimal(buy.close), 4, RoundingMode.UP).multiply(new BigDecimal(100))
              if(change.compareTo(new BigDecimal(ParseCammandParam.param.wrate)) >= 0){
                st = true //算入手续费
              }
              s"${e.trade_date}【${change}】【卖出】"
            }).mkString(", ")

            if(st){
              //记录成功的
              victoryStockList += chooseStockDto
            }
            
            val ok = if (st) "" else "X"
            
            val name =  s"<a href='${chooseStockDto.tsStock.eastmoneyURL}'>${buy.name}</a>"
            
            val buyReason = s"<span>买入</span>"

            val line = s"${modClsName}, ${buy.ts_code}, ${name},【${chooseStockDto.totalMV}亿，${chooseStockDto.limitUp}，${chooseStockDto.limitDown}，${chooseStockDto.turnoverRate}，涨跌${chooseStockDto.preChangeRate}】, ${chooseStockDto.tradedate}【${buyReason}】, ${highStr}, ${ok}"
            lines += line

            val map = new util.HashMap[String, String]()
            map.put("stockType", modClsName.toUpperCase.trim) //模型类型
            map.put("stockCode", buy.ts_code.trim)  //股票代码
            map.put("stockName", buy.name.trim)   //股票名称
            map.put("tradedate", buy.trade_date.trim)   //交易日期
            backTestMapList += map
            
          }
          
        }
      })

      if(victoryStockList.groupBy(e=>e.tradedate+e.tsStock.ts_code).filter(_._2.size>1).size>0){
        println("日期股票重复计算")
      }
      
      //计算胜率
      val victoryRate = new BigDecimal(victoryStockList.size).divide(new BigDecimal(modList.size), 4, RoundingMode.UP)
      //胜率保存到properties中
      Dataset_module_history_rtk_dir.getProperties().put(modClsName.toUpperCase, victoryRate.toString)
      val line = s"${modClsName}胜率：${victoryRate}, ${iModel.desc()}"
      lines += line
      println(line)

      linesMap.put(modClsName, lines)
      
    })


    //保存胜率
    Dataset_module_history_rtk_dir.storeProperties()

    //回测结果按模型类型分别存储
    PassFactory.engineModuleList().map(_.getClass.getSimpleName.toUpperCase).foreach(clsName=>{
      val btList = backTestMapList.filter(m=>{
          m.get("stockType") !=null
            && m.get("stockType").toUpperCase.equals(clsName.toUpperCase)
        })
        .sortBy(m=>{
          s"${m.get("tradedate")}-${m.get("stockCode")}"
        })
        .reverse
        .map(m=>{
          val l = JSONObject.toJSONString(m, Feature.LargeObject)
          l
        }).asJava
      val btResPath = ParseCammandParam.param.engineInfo.MODEL_BACK_TEST_RESULT_dir + File.separator + clsName
      FileUtils.writeLines(new File(btResPath), btList)
      println(s"${clsName}模型回测结果写入路径:${btResPath}")
    })

    linesMap.entrySet().asScala.foreach(entry=>{
      val key = entry.getKey
      val lines = entry.getValue
      sendMail(key, lines.mkString("<br>\n"))
    })

  }

  /**
   * 保存增量分析的结果集
   */
  def saveIncrementBacktestModuleResult(iModelList: List[IModel]): Unit = {
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
          val e = JSONObject.toJSONString(jsonObj, Feature.LargeObject)
          e
        }).asJava

        FileUtils.writeLines(btResFile, btList)
        println(s"${clsName}模型回测结果写入路径:${btResFile.getAbsolutePath}")
        
      })
  }

  /** *
   * 保存全量分析的结果集
   */
  def saveFullBacktestModuleResult(iModelList: List[IModel]): Unit = {
    
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
          map
        }).sortBy(m=>{
          s"${m.get("tradedate")}-${m.get("stockCode")}"
        }).reverse
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
