package zuk.tu_share.module

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.backtest.BackTestDto
import zuk.tu_share.{DataFrame, ParseCammandParam}
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.utils.ListOrderCheck

import java.io.File
import java.math.{BigDecimal, RoundingMode}
import java.nio.charset.Charset
import java.util
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object MA7_Model {

  val modelBlackTestResultList = ListBuffer[BackTestDto]()
  val modelBlackTestResultMap = new util.HashMap[String, List[BackTestDto]]()

  load()

  def load(): Unit = synchronized {
    if(modelBlackTestResultList==null || modelBlackTestResultList.isEmpty){
      val modelBlckTestResultPath = ParseCammandParam.param.path + File.separator + "MODEL_BACK_TEST_RESULT.txt"
      val modelBlckTestResultFile = new File(modelBlckTestResultPath)
      val lines = FileUtils.readLines(modelBlckTestResultFile, Charset.forName("UTF-8"))
      lines.asScala.foreach(line=>{
        try {
          val jsonObj = JSONObject.parseObject(line)
          if (jsonObj != null
            && jsonObj.get("stockType") != null
            && jsonObj.get("stockType").toString.toUpperCase.equals(classOf[MA7_Model].getSimpleName.toUpperCase)) {
            val backTestDto = new BackTestDto
            backTestDto.stockType = jsonObj.get("stockType").toString
            backTestDto.stockCode = jsonObj.get("stockCode").toString
            backTestDto.stockName = jsonObj.get("stockName").toString
            backTestDto.tradedate = jsonObj.get("tradedate").toString
            modelBlackTestResultList += backTestDto
          }
        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
        }

      })
      modelBlackTestResultList.groupBy(_.stockCode).foreach(tp2=>{
        val stockCode = tp2._1
        val ls = tp2._2.sortBy(e=>(e.tradedate)).reverse.toList
        val list = if(ls.size>5){
          ls.take(5)
        } else {
          ls
        }
        modelBlackTestResultMap.put(stockCode, list)
      })
    }

  }
}

/***
 * 底部放巨量
 */
class MA7_Model extends IModel {

  var num = 60
  var stockDto: StockDto = _

  override def getStockDto(): StockDto = stockDto

  override def run(days1: List[ModuleDay]): Unit = {

    num = days1.length
    val head = days1.head

    val list = MA7_Model.modelBlackTestResultMap.get(head.ts_code)
    val days = if(list!= null && list.size > 0){
      val ls = days1.filter(e=>{
        !list.map(_.tradedate).contains(e.trade_date)
      })
      ls
    }
    else{
      days1
    }

    if (days.size < num){
      return
    }

    val historyDays = days.slice(1, days.length)
    //过去历史最高价
    val historyHigh = historyDays.map(_.high.toFloat).max
    //过去历史最低价
    val historyLow = historyDays.map(_.low.toFloat).min
    //平均交易量
    val avgVol = new BigDecimal(historyDays.map(_.vol.toFloat).sum).divide(new BigDecimal(historyDays.size), 2, RoundingMode.UP).floatValue()

    //最近20天
    val recentDays = days.slice(1, 20)
    val recentAvgVol = new BigDecimal(recentDays.map(_.vol.toFloat).sum).divide(new BigDecimal(recentDays.size), 2, RoundingMode.UP).floatValue()

    //相比最高价，跌去的比例
    val downRate = new BigDecimal(historyHigh - head.low.toFloat).divide(new BigDecimal(historyHigh),2,RoundingMode.UP)
    //相比最高加，涨幅的比例
    val upRate = new BigDecimal(head.high.toFloat - historyLow).divide(new BigDecimal(historyLow),2,RoundingMode.UP)
    //交易量的比例
    val recentVolRate = new BigDecimal(head.vol).divide(new BigDecimal(recentAvgVol), 2, RoundingMode.UP).floatValue() //

    if(downRate.floatValue() > 0.4 //跌超4个点
//      && upRate.floatValue() < 0.5 //涨幅小于5个点
      && recentVolRate > 2.0 //放量2倍
      && ListOrderCheck.isDecreasing(recentDays.reverse.map(_.ma.ma30.floatValue()))
      && ListOrderCheck.isDecreasing(recentDays.reverse.map(_.ma.ma20.floatValue()))
      && head.ma.ma30.floatValue() > head.ma.ma20.floatValue()
      //      && head.ma.ma20.floatValue() > List(head.ma.ma10.floatValue(), head.ma.ma5.floatValue()).max
      && head.change.toFloat > 2.0
    ){

      val tsStock = DataFrame.STOCKS_MAP.get(days.head.ts_code).getOrElse(null)
      val head = days.head
      stockDto = new StockDto(tsStock, super.limitUp(days), super.limitDown(days), super.changeUpRate(days))
      stockDto.warningUpperShadow = super.upperShadow(days)


      if (StringUtils.isNotBlank(head.total_mv)) {
        stockDto.totalMV = new BigDecimal(head.total_mv).divide(new BigDecimal(10000), 2, RoundingMode.UP).floatValue()
        stockDto.preChangeRate = new BigDecimal(head.change).setScale(2, RoundingMode.HALF_UP).floatValue()
      }
      else {
        stockDto.totalMV = new BigDecimal(days(1).total_mv).divide(new BigDecimal(10000), 2, RoundingMode.UP).floatValue()
        stockDto.preChangeRate = new BigDecimal(days(1).change).setScale(2, RoundingMode.HALF_UP).floatValue()
      }

    }

  }



  override def desc(): String = "底部放巨量"

  override def winRate: Float = {
    val v = DataFrame.properties.get(classOf[MA7_Model].getSimpleName.toUpperCase)
    if (v != null) {
      v.toString.toFloat
    }
    else {
      0.8818
    }
  }

  override def reference: Float = 0.0

  override def warnUpperShadow: Boolean = false

  override def backTestStep: Int = 5
}
