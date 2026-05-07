package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.beans.BeanProperty

case class MoneyflowDto() {

  @BeanProperty private var ts_code: String = ""      // str TS代码
  @BeanProperty private var trade_date: String = ""   // str 交易日期
  @BeanProperty private var buy_sm_vol: Int = 0       // int 小单买入量（手）
  @BeanProperty private var buy_sm_amount: Float = 0  // float 小单买入金额（万元）
  @BeanProperty private var sell_sm_vol: Int = 0       // int 小单卖出量（手）
  @BeanProperty private var sell_sm_amount: Float = 0  // float 小单卖出金额（万元）
  @BeanProperty private var buy_md_vol: Int = 0         // int 中单买入量（手）
  @BeanProperty private var buy_md_amount: Float = 0    // float 中单买入金额（万元）
  @BeanProperty private var sell_md_vol: Int = 0        // int 中单卖出量（手）
  @BeanProperty private var sell_md_amount: Float = 0   // float 中单卖出金额（万元）
  @BeanProperty private var buy_lg_vol: Int = 0         // int 大单买入量（手）
  @BeanProperty private var buy_lg_amount: Float = 0    // float 大单买入金额（万元 ）
  @BeanProperty private var sell_lg_vol: Int = 0        // int 大单卖出量（手）
  @BeanProperty private var sell_lg_amount: Float = 0   // float 大单卖出金额（万元 ）
  @BeanProperty private var buy_elg_vol: Int = 0        // int 特大单买入量（手）
  @BeanProperty private var buy_elg_amount: Float = 0   // float 特大单买入金额（万元 ）
  @BeanProperty private var sell_elg_vol: Int = 0       // int 特大单卖出量（手）
  @BeanProperty private var sell_elg_amount: Float = 0  // float 特大单卖出金额（万元）
  @BeanProperty private var net_mf_vol: Int = 0         // int 净流入量（手）
  @BeanProperty private var net_mf_amount: Float = 0    // float 净流入额（万元）

}

/***
 * 加载历史资金流数据
 */
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

    val list = file.listFiles().toList.sortBy(_.getName).reverse
    val list10 = if(list.size>10) list.take(10) else list

    val records = list10.map(f=>{
      val filename = f.getName
      val in = new FileReader(f.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      records
    })

    println("")
  }

}
