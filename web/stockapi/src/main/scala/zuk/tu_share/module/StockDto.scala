package zuk.tu_share.module

import zuk.tu_share.dto.TsStock

import scala.beans.BeanProperty

class StockDto(@BeanProperty var tsStock: TsStock,  //个股详情
               @BeanProperty var limitUp: String,  //过去是否存在涨停情况
               @BeanProperty var limitDown: String,  //过去是否存在跌停情况
               @BeanProperty var turnoverRate: Float = 0.0  //过去30个交易日中，换手率大于3.5的比例
              ) {
  @BeanProperty var totalMV: Float = 0.0 //总市值
  @BeanProperty var preChangeRate: Float = 0.0 //买入当天尾盘涨跌幅
  @BeanProperty var warningUpperShadow: Boolean = false //上影线警告
}
