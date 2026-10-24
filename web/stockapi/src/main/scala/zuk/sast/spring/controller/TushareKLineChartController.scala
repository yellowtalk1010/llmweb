package zuk.sast.spring.controller

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import zuk.tu_share.dto.ModuleHistoryDailyData
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, Dataset_module_history_rtk_dir}

import java.math.RoundingMode
import java.util
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
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
class TushareKLineChartController {

  private val log = LoggerFactory.getLogger(classOf[TushareKLineChartController])

  @GetMapping(value = Array("list"))
  def list(stockCode: String, tradedate: String): util.Map[String, Object] = {

    val LEN = 20

    val stockName = Dataset_all_stocks_csv_file.getTsStock(stockCode).name

    log.info(s"${stockCode}, ${stockName}, ${tradedate}")

    val map = Dataset_module_history_rtk_dir.loadModelAnalysisDataSet
    
    if(map.get(stockCode).isEmpty){
      log.info("可能是新股、次新股、或者上市时间不足100天")
      val result = new util.HashMap[String, Object]()
      result.put("data", List.empty.asJava)
      result.put("code", "success")
      return result
    }

    val dailydataList = map.get(stockCode).get

    val subList = if(StringUtils.isNotBlank(tradedate)){
      val ls = ListBuffer[ModuleHistoryDailyData]()  //ModuleDay   StockDailyData
      //TODO 截取
      for (index <- 0 until dailydataList.size) {
        if(dailydataList(index).trade_date.equals(tradedate)) {
          val arr = dailydataList.splitAt(index)
          val list1 = if(arr._1.size > LEN * 2){
            arr._1.slice(arr._1.size - LEN * 2, arr._1.length) //
          }
          else {
            arr._1
          }
          val list2 = if(arr._2.size > LEN){
            arr._2.take(LEN)
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
      dailydataList.take(LEN * 3)
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
        dto.optType = "买"//buy买入
      }

      dto
    }).toList
    
    //todo 计算获利比例
    val ls = list.filter(_.optType.equals("买"))
    if(ls.size > 0){
      import java.math.BigDecimal
      val buy = ls.head.close
      val count = new AtomicInteger(0)
      val isSell = new AtomicBoolean(false)
      list.reverse.filter(_.time.toLong > tradedate.toLong).foreach(dto=>{
        if(count.incrementAndGet() <= 10){ //暂定10个交易日
          dto.winChange = new BigDecimal(dto.high - buy).divide(new BigDecimal(buy), 4, RoundingMode.DOWN).multiply(new BigDecimal(100)).floatValue()
          if(dto.winChange > 1.0 && !isSell.get()){
            dto.optType = "卖" + dto.winChange   //卖出
            isSell.set(true)
          }
        }
      })
    }

    val result = new util.HashMap[String, Object]()
    result.put("data", list.asJava)
    result.put("code", "success")
    result
  }

}
