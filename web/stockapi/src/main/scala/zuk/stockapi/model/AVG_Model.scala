package zuk.stockapi.model

import zuk.stockapi.{StockApiVo, StockMaVo}

import java.math.BigDecimal

/***
 * 均价模型策略
 */
class AVG_Model(stockMaVo: StockApiVo, maList: List[StockMaVo]) extends Model(stockMaVo) {

  var isOK: Boolean = false

  override def isHit(): Boolean = this.isOK

  override def run(): Unit = {
    if(maList.size>3){
      val list = maList.slice(0, 3)
      val isIncre = comIncrea(list)
      val filterList = list.filter(compMA(_))
      this.isOK = isIncre && (list.size == filterList.size)
    }
  }

  private def compMA(v0: StockMaVo): Boolean = {
    val avg5 = new BigDecimal(v0.getAvg5)
    val avg10 = new BigDecimal(v0.getAvg10)
    val avg20 = new BigDecimal(v0.getAvg20)
    val avg30 = new BigDecimal(v0.getAvg30)
    avg5.compareTo(avg10) > 0
      && avg10.compareTo(avg20) > 0
      && avg20.compareTo(avg30) > 0
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
