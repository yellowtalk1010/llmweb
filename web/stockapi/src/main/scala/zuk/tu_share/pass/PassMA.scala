package zuk.tu_share.pass
import zuk.stockapi.{StockDayVo, StockMaVo}
import zuk.tu_share.dto.{MA, ModuleDay}

import java.math
import java.math.BigDecimal
import scala.collection.mutable.ListBuffer

class PassMA extends IPass{

  override def handle(moduleDays: List[ModuleDay]): Unit = {
    calStockMA(moduleDays)
  }


  /**
   * 计算MA和AVG
   */
  def calStockMA(moduleDaySorted: List[ModuleDay]): List[ModuleDay] = {

    val newModuleDayList = ListBuffer[ModuleDay]()
    for (index <- 0 until moduleDaySorted.size - 31) {

      val subList = moduleDaySorted.slice(index, index + 30)
      val stockDayVo = subList.head

      val ma = new MA()

      ma.ma5 = this.cala_ma(subList.take(5))
      ma.ma10 = this.cala_ma(subList.take(10))
      ma.ma20 = this.cala_ma(subList.take(20))
      ma.ma30 = this.cala_ma(subList.take(30))

      ma.avg5 = this.cala_avg(subList.take(5))
      ma.avg10 = this.cala_avg(subList.take(10))
      ma.avg20 = this.cala_avg(subList.take(20))
      ma.avg30 = this.cala_avg(subList.take(30))

      stockDayVo.ma = ma

//      newModuleDayList += stockDayVo
    }
    newModuleDayList.toList
  }


  private def cala_ma(stockDayVoList: List[ModuleDay]): BigDecimal = {
    //收盘价的平均值
    val optSum = stockDayVoList.map(e=>{
      new BigDecimal(e.close)
    }).reduceOption((a,b)=>a.add(b))
    val ma = optSum.get.divide(new BigDecimal(stockDayVoList.size),5, BigDecimal.ROUND_HALF_UP)
    ma
  }

  private def cala_avg(stockDayVoList: List[ModuleDay]): BigDecimal = {
    //日均价
    val volume = stockDayVoList.filter(_.vol!=null).map(e=>{new BigDecimal(e.vol)}).reduceOption((a,b)=>a.add(b)).get
    val amount = stockDayVoList.filter(_.amount!=null).map(e=>{new BigDecimal(e.amount)}).reduceOption((a,b)=>a.add(b)).get
    if(volume.compareTo(math.BigDecimal.ZERO)==0 || amount.compareTo(math.BigDecimal.ZERO) == 0){
      math.BigDecimal.ZERO
    }
    else {
      val avg = amount.divide(volume, 5, BigDecimal.ROUND_HALF_UP);
      avg
    }
  }

}
