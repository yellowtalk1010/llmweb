package zuk.sast.controller.component

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
  @BeanProperty
  var stock_analysis_system_path: String = null

  def get_stock_result_json_path: String = this.stock_analysis_system_path + File.separator + "result_json"
  def get_stock_all_stocks_csv_path: String = this.stock_analysis_system_path + File.separator + "all_stocks.csv"



  @PostConstruct
  def init() = {
    log.info("初始化 application.properties ")

    val file = new File(this.stock_analysis_system_path)
    if(file.exists()){
      log.info(s"股票分析系统路径:${stock_analysis_system_path}, ${file.exists()}")
    }
    else {
      log.info(s"股票分析系统路径:${stock_analysis_system_path}，路径错误")
      System.exit(1)
    }

  }





}
