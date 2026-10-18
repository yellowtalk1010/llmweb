package zuk.sast.spring.controller

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.sast.spring.controller.component.{MoneyflowDCDto, TushareMoneyFlowComponent}
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.StockDailyData
import zuk.tu_share.utils.Dataset_stock_dailydata_dir

import java.math.RoundingMode
import java.util
import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*


private case class KLineChartDto() {
  @BeanProperty var stockName: String = ""
  @BeanProperty var time: String = ""
  @BeanProperty var open: Float = 0.0
  @BeanProperty var high: Float = 0.0
  @BeanProperty var low: Float = 0.0
  @BeanProperty var close: Float = 0.0
  @BeanProperty var vol: Float = 0.0
  @BeanProperty var change: Float = 0.0
  @BeanProperty var amount: Float = 0.0
  @BeanProperty var optType: String = "" //操作类型 buy或者sell

  @BeanProperty var winChange: Float = 0.0 //盈利比例
}

/***
 * 获取蜡烛图信息列表
 */
@RestController
@RequestMapping(value = Array("klineChart"))
@Component
class KLineChartController {

  private val log = LoggerFactory.getLogger(classOf[KLineChartController])

  @GetMapping(value = Array("list"))
  def list(stockCode: String, tradedate: String): util.Map[String, Object] = {

    val stockName = DataFrame.STOCKS_MAP.get(stockCode).name

    log.info(s"${stockCode}, ${stockName}, ${tradedate}")

    val dailydataList = Dataset_stock_dailydata_dir.getDailyDataList(stockCode)

    val subList = if(StringUtils.isNotBlank(tradedate)){
      val ls = ListBuffer[StockDailyData]()
      //TODO 截取
      for (index <- 0 until dailydataList.size) {
        if(dailydataList(index).trade_date.equals(tradedate)) {
          val arr = dailydataList.splitAt(index)
          val list1 = if(arr._1.size > 11){
            arr._1.slice(arr._1.size - 10, arr._1.length)
          }
          else {
            arr._1
          }
          val list2 = if(arr._2.size > 7){
            arr._2.take(6)
          }
          else{
            arr._2
          }
          ls ++= (list1 ++ list2)
        }
      }
      ls
    }
    else {
      dailydataList.take(15)
    }

    val list = subList.map(e=>{
      val dto = KLineChartDto()
      dto.stockName = e.name
      dto.time = e.trade_date
      dto.open = e.open.toFloat
      dto.high = e.high.toFloat
      dto.low = e.low.toFloat
      dto.close = e.close.toFloat
      dto.change = e.change.toFloat
      dto.vol = e.vol.toFloat
      dto.amount = e.amount.toFloat

      if(StringUtils.isNotBlank(tradedate) 
        && e.trade_date.equals(tradedate)){
        dto.optType = "buy"
      }

      dto
    }).toList
    
    //todo 计算获利比例
    val ls = list.filter(_.optType.equals("buy"))
    if(ls.size > 0){
      import java.math.BigDecimal
      val buy = ls.head.close
      list.filter(_.time.toLong > tradedate.toLong).foreach(dto=>{
        dto.winChange = new BigDecimal(dto.high - buy).divide(new BigDecimal(buy), 4, RoundingMode.DOWN).floatValue() * 100
      })
    }

    val result = new util.HashMap[String, Object]()
    result.put("data", list.asJava)
    result.put("code", "success")
    result
  }

}
