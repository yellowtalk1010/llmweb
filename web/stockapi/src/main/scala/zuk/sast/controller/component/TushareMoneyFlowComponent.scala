package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.io.File
import scala.beans.BeanProperty

@Component
class TushareMoneyFlowComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareMoneyFlowComponent])

  @Value("${stock.moneyflow.path}")
  @BeanProperty
  var moneyflowPath: String = null

  @PostConstruct
  def init(): Unit = {
    log.info(s"资金流路径：${moneyflowPath}")
    if(StringUtils.isEmpty(moneyflowPath)){
      log.error(s"资金流路径${moneyflowPath}，错误")
      System.exit(1)
    }
    val file = new File(moneyflowPath)
    if(!file.exists() || file.isFile){
      log.error(s"资金流路径${moneyflowPath}，错误")
      System.exit(1)
    }

  }

}
