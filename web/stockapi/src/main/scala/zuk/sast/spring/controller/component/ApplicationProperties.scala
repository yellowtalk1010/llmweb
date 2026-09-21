package zuk.sast.spring.controller.component

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import zuk.tu_share.ParseCammandParam

import java.io.File
import scala.beans.BeanProperty

@Component
class ApplicationProperties {

  private val log = LoggerFactory.getLogger(classOf[ApplicationProperties])
  
  
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
