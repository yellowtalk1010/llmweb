package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math.BigDecimal
import scala.collection.mutable.ListBuffer
/***
 * MA连续3天上升策略
 */
class MA_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  var isOK: Boolean = false

  override def isHit(): Boolean = this.isOK

  var stocks = new ListBuffer[StockApiVo]

  override def adviceStocks(): List[StockApiVo] = this.stocks.toList

  override def run(): Unit = {
    if(maList.size>3){
      val list = maList.slice(0, 3)
      val isIncre = comIncrea(list)
      val filterList = list.filter(compMA(_))
      this.isOK = isIncre && (list.size == filterList.size)
    }
  }

  private def compMA(v0: StockMaVo): Boolean = {
    val ma5 = new BigDecimal(v0.getMa5)
    val ma10 = new BigDecimal(v0.getMa10)
    val ma20 = new BigDecimal(v0.getMa20)
    val ma30 = new BigDecimal(v0.getMa30)
    ma5.compareTo(ma10) > 0
      && ma10.compareTo(ma20) > 0
      && ma20.compareTo(ma30) > 0
      && new BigDecimal(v0.getStockDayVo.getChangeRatio).compareTo(new BigDecimal(30.0)) < 0  //涨幅
  }

  /**
   * 收盘价是否递增
   */
  private def comIncrea(list: List[StockMaVo]): Boolean = {
    var st = true
    for(i <- 0 until list.size if st){
      val e0 = list(i)
      val ls = list.splitAt(i)._2
      val filter = ls.filter(e=>{
        new BigDecimal(e0.getStockDayVo.getClose).compareTo(new BigDecimal(e.getStockDayVo.getClose)) >= 0
      })
      st = ls.size == filter.size
    }
    st
  }

}
