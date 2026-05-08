package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.csv.{CSVFormat, CSVRecord}
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import zuk.tu_share.dto.TsStock

import java.io.{File, FileReader}
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*
import java.math.BigDecimal

case class MoneyflowDCDto() {

  @BeanProperty var trade_date: String = "" // str 交易日期
  @BeanProperty var ts_code: String = "" // str 股票代码
  @BeanProperty var name: String = "" // str 股票名称

  @BeanProperty var pct_change: String = "" // float 涨跌幅
  @BeanProperty var close: String = "" // float 最新价

  @BeanProperty var net_amount: String = "" // float 今日主力净流入额（万元）
  @BeanProperty var net_amount_rate: String = "" // float 今日主力净流入净占比（%）
  @BeanProperty var buy_elg_amount: String = "" // float 今日超大单净流入额（万元）
  @BeanProperty var buy_elg_amount_rate: String = "" // float 今日超大单净流入占比（%）
  @BeanProperty var buy_lg_amount: String = "" // float 今日大单净流入额（万元）
  @BeanProperty var buy_lg_amount_rate: String = "" // float 今日大单净流入占比（%）
  @BeanProperty var buy_md_amount: String = "" // float 今日中单净流入额（万元）
  @BeanProperty var buy_md_amount_rate: String = "" // float 今日中单净流入占比（%）
  @BeanProperty var buy_sm_amount: String = "" // float 今日小单净流入额（万元）
  @BeanProperty var buy_sm_amount_rate: String = "" // float 今日小单净流入占比（%）

}

/***
 * 加载东方财富历史资金流数据
 */
@Component
class TushareMoneyFlowComponent {

  private val log = LoggerFactory.getLogger(classOf[TushareMoneyFlowComponent])

  @Autowired
  private var tushareAllStocksCSVComponent: TushareAllStocksCSVComponent = null

  private val MAP = new ConcurrentHashMap[String, List[MoneyflowDCDto]]()

  @Value("${stock.moneyflow_dc.path}")
  @BeanProperty
  var moneyflowPath: String = null

  @PostConstruct
  def init(): Unit = {
    log.info(s"东方财富资金流路径：${moneyflowPath}")
    if(StringUtils.isEmpty(moneyflowPath)){
      log.error(s"东方财富资金流路径${moneyflowPath}，错误")
      System.exit(1)
    }
    val file = new File(moneyflowPath)
    if(!file.exists() || file.isFile){
      log.error(s"东方财富资金流路径${moneyflowPath}，错误")
      System.exit(1)
    }

    val fileList = file.listFiles().toList.sortBy(_.getName).reverse
    val fileList10 = if(fileList.size>10) fileList.take(10) else fileList

    val fileRecordList = fileList10.map(f=>{
      val filename = f.getName
      val in = new FileReader(f.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

      val moneyflowDtoList = records.asScala.map(record=>{
        val dto = new MoneyflowDCDto

        dto.ts_code = record.get("ts_code")
        dto.name = record.get("name")
        dto.trade_date = record.get("trade_date")


        dto.net_amount = record.get("net_amount")
        dto.net_amount_rate = record.get("net_amount_rate")
        dto.buy_elg_amount = record.get("buy_elg_amount")
        dto.buy_elg_amount_rate = record.get("buy_elg_amount_rate")
        dto.buy_lg_amount = record.get("buy_lg_amount")
        dto.buy_lg_amount_rate = record.get("buy_lg_amount_rate")

        dto.buy_md_amount = record.get("buy_md_amount")
        dto.buy_md_amount_rate = record.get("buy_md_amount_rate")
        dto.buy_sm_amount = record.get("buy_sm_amount")
        dto.buy_sm_amount_rate = record.get("buy_sm_amount_rate")

        dto
      })

      in.close()

      moneyflowDtoList.toList
    })

    fileRecordList.flatMap(l=>l).groupBy(_.ts_code).foreach(e=>{
      val tsCode = e._1
      val ls = e._2.sortBy(_.trade_date).reverse
      MAP.put(tsCode, ls)
    })

    println(s"完成历史资金流初始化，${MAP.size()}")
  }

  def getTsCode(tscode: String): List[MoneyflowDCDto] = {
    val v = MAP.get(tscode)
    if(v==null){
      List.empty
    }
    else {
      v
    }
  }

}
