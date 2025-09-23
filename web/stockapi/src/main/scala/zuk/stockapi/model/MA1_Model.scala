package zuk.stockapi.model

import org.apache.commons.lang3.StringUtils
import zuk.stockapi.utils.CloseIncreaseUtil
import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math.BigDecimal
import scala.collection.mutable.ListBuffer

/***
 * 刚刚M5>M10>M20>M30
 */
class MA1_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  var turnoverRatio = 4 //默认换手率
  var isOK: Boolean = false

  override def isHit(): Boolean = this.isOK

  override def run(): Unit = {
    val len = 3
    if(maList.size > len){
      val list = maList.slice(0, len)
      val today = list.head
      val isIncre = CloseIncreaseUtil.comIncrea(list)
      if (comp(today)
        && isIncre
        && compTrunoverRatio(today)
        && compChangeRatio(today)
      ) {
        val filterList = list.filter(comp(_))
        isOK = list.size != filterList.size
      }
    }
  }

  /** *
   * 比较换手率
   *
   * @param today
   * @return
   */
  private def compTrunoverRatio(today: StockMaVo): Boolean = {
    if (StringUtils.isNotBlank(today.getStockDayVo.getTurnoverRatio)) {
      val st = new BigDecimal(today.getStockDayVo.getTurnoverRatio).compareTo(new BigDecimal(turnoverRatio)) > 0 //换手率要大于4%
      st
    }
    else {
      true
    }
  }

  /** *
   * 比较涨跌幅度
   *
   * @param today
   * @return
   */
  private def compChangeRatio(today: StockMaVo): Boolean = {
    if (StringUtils.isNotBlank(today.getStockDayVo.getChangeRatio)) {
      val st = new BigDecimal(today.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(2)) > 0 //涨幅大于3%
      st
    }
    else {
      true
    }

  }

  private def comp(v0: StockMaVo): Boolean = {
    val ma5 = new BigDecimal(v0.getMa5)
    val ma10 = new BigDecimal(v0.getMa10)
    val ma20 = new BigDecimal(v0.getMa20)
    val ma30 = new BigDecimal(v0.getMa30)
    ma5.compareTo(ma10) > 0
      && ma10.compareTo(ma20) > 0
      && ma20.compareTo(ma30) > 0
  }

}
