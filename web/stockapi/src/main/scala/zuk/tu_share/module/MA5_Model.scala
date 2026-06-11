package zuk.tu_share.module

import zuk.tu_share.dto.ModuleDay

class MA5_Model extends IModel {

  private var stockDto: StockDto = _

  override def run(days: List[ModuleDay]): Unit = {
    val ma4 = new MA4_Model()
    ma4.run(days)
    val ma4Stock = ma4.getStockDto()
    //
    if(ma4Stock!=null){
      List(new MA1_Model, new MA1_1_Model, new MA3_0_Model, new MA3_1_Model, new MA3_2_Model, new MA3_3_Model).foreach(e => {
        if(stockDto==null) {
          e.run(days)
          stockDto = e.getStockDto()
        }
      })
    }

  }

  override def getStockDto(): StockDto = stockDto

  override def desc(): String = "击中"

  override def winRate: Float = 0.0

  override def reference: Float = 0.0

  override def warnUpperShadow: Boolean = false
}
