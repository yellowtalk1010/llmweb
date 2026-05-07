package zuk.sast.controller

import jakarta.annotation.PostConstruct
import org.apache.commons.csv.CSVFormat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.beans.BeanProperty

/***
 * 股票资金流向
 */
@RestController
@RequestMapping(value = Array("money_flow"))
@Component
class TushareMoneyFlowController {

  private val log = LoggerFactory.getLogger(classOf[TushareMoneyFlowController])

  @Value("${stock.moneyflow.path}")
  @BeanProperty
  var moneyflowPath: String = null

  @PostConstruct
  def init(): Unit = {
    log.info(s"tushare资金流路径：${moneyflowPath}")
    val file = new File(moneyflowPath)
    if(file.exists() && file.isDirectory){
      file.listFiles().toList.sortBy(_.getName).reverse.map(f=>{
        val filename = f.getName
        val in = new FileReader(f.getAbsolutePath, Charset.forName("UTF-8"))
        val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
        (filename, records)
      })


    }
    else {
      log.error(s"资金流路径错误：${moneyflowPath}")
    }

  }

}
