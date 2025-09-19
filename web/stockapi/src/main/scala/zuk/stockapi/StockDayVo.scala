package zuk.stockapi

import scala.beans.BeanProperty

class StockDayVo (@BeanProperty code: String,
                  @BeanProperty time: String,
                  @BeanProperty open: String,
                  @BeanProperty turnoverRatio: String,
                  @BeanProperty amount: String,
                  @BeanProperty high: String,
                  @BeanProperty low: String,
                  @BeanProperty changeRatio: String,
                  @BeanProperty close: String,
                  @BeanProperty volume: String
)
