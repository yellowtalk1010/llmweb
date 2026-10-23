package zuk

import zuk.tu_share.backtest.BackTest
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.module.{IModel, MA4_Model, MA5_Model, MA7_1_Model, MA7_Model, MA8_Model, MA9_1_Model, MA9_Model}
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.pass.PassFactory.engineModuleList
import zuk.tu_share.send.ISendFactory
import zuk.tu_share.utils.{Dataset_module_history_rtk_dir, LicenseUtil}

import java.io.PrintStream
import java.nio.charset.{Charset, StandardCharsets}
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer

object Main {

  def main(args: Array[String]): Unit = {
    
    // 设置默认编码
    fixWindowsConsole()
    //参数解析
    ParseCammandParam.parse(args)
    println(s"${ParseCammandParam.param.toString}")
    //许可密码校验
    if(!LicenseUtil.checkPwd()){
      println("pwd err")
      System.exit(0)
    }
    if (ParseCammandParam.param.back){
      //回测 
      println("全量回测")

      //选择需要分析的模型
      val modNameSet = Array(
        classOf[MA4_Model],
        classOf[MA5_Model],
        classOf[MA7_Model],
        classOf[MA7_1_Model],
        classOf[MA8_Model],
        classOf[MA9_Model],
        classOf[MA9_1_Model]
      ).map(_.getSimpleName.toUpperCase).toSet

      val map = Dataset_module_history_rtk_dir.loadModelAnalysisDataSet.filter(_._2.size > 100)
      for (i <- 0 to ParseCammandParam.param.back_step) {

        PassFactory.doModule(modNameSet, map, i)

        println(s">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${i}")
      }
      
      map.toList.foreach(tp2=>{
        val stockCode = tp2._1 //股票代码
        val stockName = tp2._2.head.name //股票名称

        //全量回测
        val funllBacktestFinishModules = ListBuffer[IModel]()
        for (i <- 0 to ParseCammandParam.param.back_step){
          val stockDataList = tp2._2.slice(i, tp2._2.size) //股票数据集 
          println(s"全量回测${stockCode}, ${stockName}, ${stockDataList.last.trade_date} 至 ${stockDataList.head.trade_date}, 共${stockDataList.size}条")
          //全量回测的模型
          val fullBacktestModNameSet = Array(
            classOf[MA4_Model],
            classOf[MA5_Model],
            classOf[MA7_1_Model],
            classOf[MA8_Model],
            classOf[MA9_1_Model]
          ).map(_.getSimpleName.toUpperCase).toSet
          val fullBacktestModules = engineModuleList().filter(m => fullBacktestModNameSet.map(_.toUpperCase).contains(m.getClass.getSimpleName.toUpperCase))
          PassFactory.doModule_1(fullBacktestModules, stockDataList)
          funllBacktestFinishModules ++= fullBacktestModules.filter(e => e.getStockDto() != null && e.getStockDto().tsStock != null)
        }
        
        //增量回测
        val incrementBacktestFinishModules = ListBuffer[IModel]()
        for (i <- 0 to 30) {
          //增量回测的模型，仅增量最近30个交易日的
          val stockDataList = tp2._2.slice(i, tp2._2.size) //股票数据集
          println(s"增量回测${stockCode}, ${stockName}, ${stockDataList.last.trade_date} 至 ${stockDataList.head.trade_date}, 共${stockDataList.size}条")
          val incrementBacktestModNameSet = Array(
            classOf[MA7_Model],
            classOf[MA9_Model]
          ).map(_.getSimpleName.toUpperCase).toSet
          val incrementBacktestModules = engineModuleList().filter(m => incrementBacktestModNameSet.map(_.toUpperCase).contains(m.getClass.getSimpleName.toUpperCase))
          PassFactory.doModule_1(incrementBacktestModules, stockDataList)
          incrementBacktestFinishModules ++= incrementBacktestModules.filter(e => e.getStockDto() != null && e.getStockDto().tsStock != null)

        }
        
      })

      BackTest.analysis_full(map)
      println("完成全量回测分析")

      System.exit(1)
    }
    else {

      //选择需要分析的模型
      val modNameSet = Array(
        classOf[MA4_Model],
        classOf[MA5_Model],
        classOf[MA7_Model],
        classOf[MA7_1_Model],
        classOf[MA8_Model],
        classOf[MA9_Model],
        classOf[MA9_1_Model]
      ).map(_.getSimpleName.toUpperCase).toSet

      //加载数据
      val map = Dataset_module_history_rtk_dir.loadModelAnalysisDataSet.filter(_._2.size>100)
//      PassFactory.doModule(modNameSet, map)

      val count = new AtomicInteger(0)
      val finishModules = ListBuffer[IModel]()
      map.toList.foreach(tp2=>{
        val stockCode = tp2._1 //股票代码
        val stockName = tp2._2.head.name //股票名称
        val stockDataList = tp2._2 //股票数据集
        //输出进度
        println(s"${stockCode}, ${stockName}, ${stockDataList.last.trade_date} 至 ${stockDataList.head.trade_date}, 共${stockDataList.size}条，进度：${count.incrementAndGet()}/${map.size}")
        //模型分析
        val modules = engineModuleList().filter(m => modNameSet.map(_.toUpperCase).contains(m.getClass.getSimpleName.toUpperCase))
        PassFactory.doModule_1(modules, stockDataList)
        finishModules ++= modules.filter(e => e.getStockDto() != null && e.getStockDto().tsStock != null)
      })

      //发送结果
      ISendFactory.doSend(finishModules.toList)

      println("完成模型引擎分析")

      System.exit(1)
    }

  }


  private def fixWindowsConsole(): Unit = {
    try {
      // Windows 控制台默认是 GBK/GB2312
      val encoding = "UTF-8"
      System.setProperty("file.encoding", encoding)
      // 获取系统默认编码
      val defaultCharset = Charset.forName(encoding)
      System.setOut(new PrintStream(System.out, true, defaultCharset))
      System.setErr(new PrintStream(System.err, true, defaultCharset))
      // 设置中文 locale
      Locale.setDefault(Locale.CHINA)
    } catch {
      case e: Exception =>
    }
  }

}
