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
  
  def analysis_1(map: mutable.HashMap[String, List[ModuleHistoryDailyData]]): Unit = {
    
    val linesMap = new util.HashMap[String, ListBuffer[String]]()
    val backTestMapList = new ListBuffer[util.HashMap[String, String]]
    
    
    val memoryList = ToBackTest.memoryBackTestList
    val modGroupMap = memoryList.groupBy(_.getClass.getSimpleName)
    modGroupMap.toList.foreach(tp2=>{
      
      val modClsName = tp2._1 //模型类名
      val iModel = PassFactory.engineModuleList().filter(_.getClass.getSimpleName.equals(modClsName)).head

      val lines = new ListBuffer[String]()
      
      //模型筛选的股票按时间排序
      val modChooseStockList = tp2._2.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null).map(_.getStockDto()).sortBy(e=>(e.tradedate, e.tsStock.ts_code)).reverse
      
      //模型筛选的股票回测成功的股票集
      val victoryStockList = ListBuffer[StockDto]()
      
      modChooseStockList.foreach(chooseStockDto=>{

        if(!Dataset_module_history_rtk_dir.loadModelAnalysisDataSet.get(chooseStockDto.tsStock.ts_code).isEmpty){
          val dailyList = map.get(chooseStockDto.tsStock.ts_code).get
          
          //todo 构建买点
          val buy = dailyList.filter(_.trade_date.equals(chooseStockDto.tradedate)).head
          val buyFutureList = dailyList.filter(_.trade_date.toLong > chooseStockDto.tradedate.toLong)
          
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
            backTestMapList += map
            
          }
          else {
            
            var st = false
            val preClose = sellList.head.pre_close
            val highStr = sellList.map(e => {
              val change = ((new BigDecimal(e.high).subtract(new BigDecimal(preClose))).multiply(new BigDecimal(100))).divide(new BigDecimal(preClose), 4, RoundingMode.UP)
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

      //计算胜率
      val victoryRate = new BigDecimal(victoryStockList.size).divide(new BigDecimal(modChooseStockList.size), 4, RoundingMode.UP)
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

  def analysis(): Unit = {

    val linesMap = new util.HashMap[String, ListBuffer[String]]()
    val backTestMapList = new ListBuffer[util.HashMap[String, String]]

    ToBackTest.memoryBackTestList.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null)
      .groupBy(_.getClass.getSimpleName)
      .filter(_._2.size>0)
      .foreach(e=>{
        val clsName = e._1
        val ls = e._2
        val lines = new ListBuffer[String]()
        val victoryList = ls.sortBy(e=>(e.buy.trade_date, e.getStockDto().preChangeRate)).reverse.filter(mod=>{
            if(mod.sells.size==0){
              val tsStock = Dataset_all_stocks_csv_file.getTsStock(mod.buy.ts_code)
              val name = if(tsStock!=null){
                val url = tsStock.eastmoneyURL
                val href = s"<a href='${url}'>${mod.buy.name}</a>"
                href
              }
              else {
                mod.buy.name
              }

              val buyReason = s"<span title='${mod.buyReason()}'>买入</span>"

              val line = s"${clsName}, ${mod.buy.ts_code}, ${name},【${mod.getStockDto().totalMV}亿，${mod.getStockDto().limitUp}，${mod.getStockDto().limitDown}，${mod.getStockDto().turnoverRate}，涨跌${mod.getStockDto().preChangeRate}】, ${mod.buy.trade_date}【${buyReason}】, 【未交易】"
              lines += line


              val map = new util.HashMap[String, String]()
              map.put("stockType", clsName.toUpperCase.trim)
              map.put("stockCode", mod.buy.ts_code.trim)
              map.put("stockName", mod.buy.name.trim)
              map.put("tradedate", mod.buy.trade_date.trim)
              backTestMapList += map

            }
            mod.sells.size>0
          }).filter(mod => {

            var st = false
            val preClose = mod.sells.head.pre_close
            val highStr = mod.sells.map(e => {
              val change = ((new BigDecimal(e.high).subtract(new BigDecimal(preClose))).multiply(new BigDecimal(100))).divide(new BigDecimal(preClose), 4, RoundingMode.UP)
              if(change.compareTo(new BigDecimal(ParseCammandParam.param.wrate)) >= 0){
                st = true //算入手续费
              }
              s"${e.trade_date}【${change}】【卖出】"
            }).mkString(", ")

            val ok = if (st) "" else "X"

            val tsStock = Dataset_all_stocks_csv_file.getTsStock(mod.buy.ts_code)
            val name = if (tsStock!=null) {
              val url = tsStock.eastmoneyURL
              val href = s"<a href='${url}'>${mod.buy.name}</a>"
              href
            }
            else {
              mod.buy.name
            }

            val buyReason = s"<span title='${mod.buyReason()}'>买入</span>"

            val line = s"${clsName}, ${mod.buy.ts_code}, ${name},【${mod.getStockDto().totalMV}亿，${mod.getStockDto().limitUp}，${mod.getStockDto().limitDown}，${mod.getStockDto().turnoverRate}，涨跌${mod.getStockDto().preChangeRate}】, ${mod.buy.trade_date}【${buyReason}】, ${highStr}, ${ok}"
            lines += line

            val map = new util.HashMap[String, String]()
            map.put("stockType", clsName.toUpperCase.trim) //模型类型
            map.put("stockCode", mod.buy.ts_code.trim)  //股票代码
            map.put("stockName", mod.buy.name.trim)   //股票名称
            map.put("tradedate", mod.buy.trade_date.trim)   //交易日期
            backTestMapList += map

            st
        })

        //计算胜率
        val victoryRate = new BigDecimal(victoryList.size).divide(new BigDecimal(ls.filter(_.sells.size>0).size), 4, RoundingMode.UP)
        //胜率保存到properties中
        Dataset_module_history_rtk_dir.getProperties().put(clsName.toUpperCase, victoryRate.toString)
        val line = s"${clsName}胜率：${victoryRate}, ${ls.head.desc()}"
        lines += line
        println(line)

        linesMap.put(clsName, lines)
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

  private def sendMail(clsName: String, htmlContent: String) = {
    val mailAddress = "513283439@qq.com"
    val tradeDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date)
    SendMail.sendSimpleEmail(mailAddress, mailAddress, s"【${clsName}】${tradeDate}【backtest】【${ParseCammandParam.param.wrate} 】", htmlContent)
  }

}
