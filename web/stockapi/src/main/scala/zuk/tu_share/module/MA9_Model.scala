package zuk.tu_share.module

import org.apache.commons.lang3.StringUtils
import zuk.tu_share.dto.{ModuleDay, TopInst}
import zuk.tu_share.utils.Dataset_top_Inst_dir
import zuk.tu_share.{DataFrame, ParseCammandParam}

import java.io.File
import java.math.{BigDecimal, RoundingMode}
import java.nio.file.{Path, Paths}
import java.util
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

/***
 * 历史最低成交量
 */
class MA9_Model extends IModel {

  var stockDto: StockDto = _
  var reason = ""

  override def buyReason(): String = this.reason

  override def getStockDto(): StockDto = {
    stockDto
  }

  override def backTestStep: Int = 5

  override def run(days: List[ModuleDay]): Unit = {
    val LEN = 20
    if(days.size > LEN){
      val minVolDay = days.slice(1, LEN-1).take(LEN).minBy(_.vol.toDouble) //最低成交
      if(minVolDay.vol.toDouble >= days.head.vol.toDouble){
        //当前成交量是历史最低成交量
        val tsStock = super.findTsStock(days.head.ts_code)
        if(tsStock!=null){
          this.reason = s"相比${minVolDay.ts_code}，历史最低，收盘前买入（涨跌停的不考虑）"
          stockDto = new StockDto(tsStock) 
        }
      }
    }
  }


  override def desc(): String = {
    "过去两个月最低缩量"
  }

  override def reference: Float = {
    0.0
  }

  override def warnUpperShadow: Boolean = {
    false
  }
}
