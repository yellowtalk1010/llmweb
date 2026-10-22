package zuk.tu_share.module

import zuk.similar.{SimilartyDto, SimilartyUtil}
import zuk.tu_share.dto.{ModuleHistoryDailyData, TsStock}
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, Dataset_module_history_rtk_dir}

import java.math
import scala.collection.mutable.ListBuffer
import java.math.{BigDecimal, RoundingMode}

trait IModel {

//  var sells = ListBuffer[ModuleHistoryDailyData]()
//  var buy: ModuleHistoryDailyData = _

  def run(days: List[ModuleHistoryDailyData]): Unit //运行模型

  def getStockDto(): StockDto

  def desc():String

  //买入理由
  def buyReason(): String = ""

  def findTsStock(tsCode: String): TsStock = {
    Dataset_all_stocks_csv_file.getTsStock(tsCode)
  }

  def winRate: Float = {
    val className = this.getClass.getSimpleName
    val v = Dataset_module_history_rtk_dir.getProperties().get(className.toUpperCase)
    if (v != null) {
      v.toString.toFloat
    }
    else {
      0.00
    }
  }

  def reference: Float

  //默认回测天数是3
  def backTestStep: Int = 3

  //上影线警告
  def warnUpperShadow: Boolean

  def filterPriceLimitUp(moduleDay: ModuleHistoryDailyData): Boolean = {
    if(moduleDay.priceLimit == null){
      true
    }
    else {
      //收盘价小于涨停价
      new BigDecimal(moduleDay.close).compareTo(moduleDay.priceLimit.priceLimitUp) < 0
    }
  }

  def limitUp(days: List[ModuleHistoryDailyData]): String = {
    try {
      val max = 30
      //历史上30天出现过涨停次数
      if (days.size > max) {
        val size = days.take(max).filter(_.change.toFloat >= 9.0).size
        s"${size}/30涨"
      }
      else {
        val size = days.filter(_.change.toFloat >= 9.0).size
        s"${size}/${days.size}涨"
      }
    }
    catch
      case exception: Exception => "0/0涨"
  }

  def limitDown(days: List[ModuleHistoryDailyData]): String = {
    try {
      val max = 30
      //历史上30天出现过涨停次数
      if (days.size > max) {
        val size = days.take(max).filter(_.change.toFloat <= -9.0).size
        s"${size}/30跌"
      }
      else {
        val size = days.filter(_.change.toFloat <= -9.0).size
        s"${size}/${days.size}跌"
      }
    }
    catch
      case exception: Exception => "0/0跌"
  }

  def changeUpRate(days: List[ModuleHistoryDailyData]): Float = {
    try{
      //换手率超过5%的比例
      if(days.size==0){
        return 0.0
      }
      val max = 30
      var size = 0
        if (days.size > max) {
          size = days.take(max).filter(_.turnover_rate.toFloat >= 5.0).size
          new BigDecimal(size).divide(new BigDecimal(max), 1, RoundingMode.UP).floatValue()
      }
      else {
        size = days.filter(_.change.toFloat >= 5.0).size
        new BigDecimal(size).divide(new BigDecimal(days.size), 1, RoundingMode.UP).floatValue()
      }
    }
    catch
      case exception: Exception => 0.0
  }

  /***
   * 上引线比例
   * @param days
   * @return
   */
  def upperShadow(days: List[ModuleHistoryDailyData]): Boolean = {
    val list = new ListBuffer[ModuleHistoryDailyData]()
    if(days.size==1){
      list += days.head
    }
    else if(days.size>=2) {
      list ++= days.take(2)
    }
    val rates = list.map(e=>{
      val closePrice = new BigDecimal(e.close)
      val highPrice = new BigDecimal(e.high)
      val lowPrice = new BigDecimal(e.low)
      if(highPrice.subtract(lowPrice).compareTo(math.BigDecimal.ZERO)==0){
        math.BigDecimal.ZERO
      }
      else {
        val upperShadowRate = (highPrice.subtract(closePrice)).divide(highPrice.subtract(lowPrice), 4, RoundingMode.DOWN)
        upperShadowRate
      }
    }).filter(e=>e.compareTo(new BigDecimal(0.35))>=0)

    rates.size>0
  }

  /***
   * 形态相似度计算
   * 
   * @return
   */
  def getSimilartyDto(): SimilartyDto = null
  
}
