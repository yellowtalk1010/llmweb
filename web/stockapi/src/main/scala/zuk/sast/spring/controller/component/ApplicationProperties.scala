package zuk.sast.spring.controller.component

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.io.File
import scala.beans.BeanProperty

@Component
class ApplicationProperties {

  private val log = LoggerFactory.getLogger(classOf[ApplicationProperties])

  /***
   * 股票分析系统路径
   */
  @Value("${stock.analysis.system.path}")
  private var stock_analysis_system_path: String = null
  //股票分析系统路径
  def getStockAnalysisSystemPath: String = this.stock_analysis_system_path
  //股票分析系统结果存储路径
  def getStockAnalysisSystem_resultJsonSavePath: String = this.stock_analysis_system_path + File.separator + "result_json"
  //股票分析系统待分析的股票详情
  def getStockAnalysisSystem_allStocksCsvPath: String = this.stock_analysis_system_path + File.separator + "all_stocks.csv"
  //股票分析系统，股票所属板块和概念路径
  def getStockAnalysisSystem_conceptPath: String = this.stock_analysis_system_path + File.separator + "concept"
  //股票分析系统，及时股票信息路径
  def getStockAnalysisSystem_rtkPath: String = this.stock_analysis_system_path + File.separator + "rt_k" + File.separator + "rt_k.csv"
  //股票分析系统，回测结果路径
  def getStockAanlysisSystem_backTestResultPath: String = this.stock_analysis_system_path + File.separator + "MODEL_BACK_TEST_RESULT.txt"


  /***
   * 股票数据源构建系统路径
   */
  @Value("${stock.datasource.build.system.path}")
  private var stock_datasource_build_system_path: String = null
  //股票数据源构建系统路径
  def getStockDatasourceBuildSystemPath: String = this.stock_datasource_build_system_path
  //龙虎榜数据存储路径
  def getStockDatasourceBuildSystem_stockHmTopInstPath: String = stock_datasource_build_system_path + "/hm/top_inst/"
  //东方财富资金流路径
  def getStockDatasourceBuildSystem_moneyflowPath: String =  stock_datasource_build_system_path + "/moneyflow/data/moneyflow_dc/"


  @PostConstruct
  def init() = {
    log.info("初始化 application.properties ")

    //
    val file = new File(this.stock_analysis_system_path)
    if(file.exists()){
      log.info(s"股票分析系统路径:${stock_analysis_system_path}, ${file.exists()}")
    }
    else {
      log.info(s"股票分析系统路径:${stock_analysis_system_path}，路径错误")
      System.exit(1)
    }

    //
    val file1 = new File(this.stock_datasource_build_system_path)
    if(file1.exists()){
      log.info(s"股票数据源构建系统路径:${stock_datasource_build_system_path}, ${file1.exists()}")
    }
    else {
      log.info(s"股票数据源构建系统路径:${stock_datasource_build_system_path}，路径错误")
      System.exit(1)
    }

  }





}
