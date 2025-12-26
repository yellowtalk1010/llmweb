package zuk.tu_share.module

import zuk.tu_share.dto.TsStock

import scala.beans.BeanProperty

class StockDto(@BeanProperty var tsStock: TsStock,  //个股详情
               @BeanProperty var limitUp: Boolean = false,  //过去是否存在涨停情况
               @BeanProperty var turnoverRate: Float = 0.0  //过去30个交易日中，换手率大于3.5的比例
              ) {

}
