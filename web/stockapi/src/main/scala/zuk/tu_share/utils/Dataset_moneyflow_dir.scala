package zuk.tu_share.utils

import jakarta.annotation.PostConstruct
import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import zuk.sast.spring.controller.component.MoneyflowDCDto
import zuk.tu_share.ParseCammandParam

import java.io.{File, FileReader}
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

/***
 * 东方财富资金流向
 */
object Dataset_moneyflow_dir {

  private val MAP = new ConcurrentHashMap[String, List[MoneyflowDCDto]]()
  
  private def load(): Unit = {
    val moneyflowPath = ParseCammandParam.param.datasetInfo.moneyflow_dc_dir
    println(s"东方财富资金流路径：${moneyflowPath}")
    if (StringUtils.isEmpty(moneyflowPath)) {
      println(s"东方财富资金流路径${moneyflowPath}，错误")
      System.exit(1)
    }
    val file = new File(moneyflowPath)
    if (!file.exists() || file.isFile) {
      println(s"东方财富资金流路径${moneyflowPath}，错误")
      System.exit(1)
    }

    val fileList = file.listFiles().toList.sortBy(_.getName).reverse
    val fileList10 = if (fileList.size > 7) fileList.take(7) else fileList

    val fileRecordList = fileList10.map(f => {
      val filename = f.getName
      val in = new FileReader(f.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

      val moneyflowDtoList = records.asScala.map(record => {
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

    fileRecordList.flatMap(l => l).groupBy(_.ts_code).foreach(e => {
      val tsCode = e._1
      val ls = e._2.sortBy(_.trade_date).reverse
      MAP.put(tsCode, ls)
    })

    println(s"完成历史资金流初始化，${MAP.size()}")
  }

  def getTsCode(tscode: String): List[MoneyflowDCDto] = synchronized {
    if(MAP.size() == 0){
      load()
    }
    val v = MAP.get(tscode)
    if (v == null) {
      List.empty
    }
    else {
      v
    }
  }

}
