package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.csv.{CSVFormat, CSVRecord}
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.io.{File, FileReader}
import java.nio.charset.Charset
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

case class MoneyflowDto() {

  @BeanProperty var ts_code: String = ""      // str TS代码
  @BeanProperty var trade_date: String = ""   // str 交易日期
  @BeanProperty var buy_sm_vol: String = ""       // int 小单买入量（手）
  @BeanProperty var buy_sm_amount: String = ""  // float 小单买入金额（万元）

  @BeanProperty var sell_sm_vol: String = ""       // int 小单卖出量（手）
  @BeanProperty var sell_sm_amount: String = ""  // float 小单卖出金额（万元）
  @BeanProperty var buy_md_vol: String = ""         // int 中单买入量（手）
  @BeanProperty var buy_md_amount: String = ""    // float 中单买入金额（万元）
  @BeanProperty var sell_md_vol: String = ""        // int 中单卖出量（手）
  @BeanProperty var sell_md_amount: String = ""   // float 中单卖出金额（万元）

  @BeanProperty var buy_lg_vol: String = ""         // int 大单买入量（手）
  @BeanProperty var buy_lg_amount: String = ""    // float 大单买入金额（万元 ）
  @BeanProperty var sell_lg_vol: String = ""        // int 大单卖出量（手）
  @BeanProperty var sell_lg_amount: String = ""   // float 大单卖出金额（万元 ）
  @BeanProperty var buy_elg_vol: String = ""        // int 特大单买入量（手）
  @BeanProperty var buy_elg_amount: String = ""   // float 特大单买入金额（万元 ）

  @BeanProperty var sell_elg_vol: String = ""       // int 特大单卖出量（手）
  @BeanProperty var sell_elg_amount: String = ""  // float 特大单卖出金额（万元）
  @BeanProperty var net_mf_vol: String = ""         // int 净流入量（手）
  @BeanProperty var net_mf_amount: String = ""    // float 净流入额（万元）

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

      val moneyflowDtos = records.asScala.map(record=>{
        val dto = new MoneyflowDto
        dto.ts_code = record.get("ts_code")
        dto.trade_date = record.get("trade_date")
        dto.buy_sm_vol = record.get("buy_sm_vol")
        dto.buy_sm_amount = record.get("buy_sm_amount")

        dto.sell_sm_vol = record.get("sell_sm_vol")
        dto.sell_sm_amount = record.get("sell_sm_amount")
        dto.buy_md_vol = record.get("buy_md_vol")
        dto.buy_md_amount = record.get("buy_md_amount")
        dto.sell_md_vol = record.get("sell_md_vol")
        dto.sell_md_amount = record.get("sell_md_amount")

        dto.buy_lg_vol = record.get("buy_lg_vol")
        dto.buy_lg_amount = record.get("buy_lg_amount")
        dto.sell_lg_vol = record.get("sell_lg_vol")
        dto.sell_lg_amount = record.get("sell_lg_amount)
        dto.buy_elg_vol = record.get("buy_elg_vol")
        dto.buy_elg_amount = record.get("buy_elg_amount")

        dto.sell_elg_vol = record.get("sell_elg_vol")
        dto.sell_elg_amount = record.get("sell_elg_amount")
        dto.net_mf_vol = record.get("net_mf_vol")
        dto.net_mf_amount = record.get("net_mf_amount")

        dto
      })

      in.close()
      moneyflowDtos
    })

//    val codes = records.map(record: CSVRecord => {
//        val moneyflowDto = new MoneyflowDto()
//        record.getn
//        moneyflowDto.ts_code = record.get("ts_code")
//        moneyflowDto
//      })
//      .toList

    println("")
  }

}
