package zuk.stockapi

import scala.beans.BeanProperty

class StockMaVo (@BeanProperty time: String,
                 @BeanProperty stockDayVo: StockDayVo,
                 @BeanProperty avg: String,
                 @BeanProperty avg5: String,
                 @BeanProperty avg10: String,
                 @BeanProperty avg20: String,
                 @BeanProperty avg30: String,
                 @BeanProperty ma5: String,
                 @BeanProperty ma10: String,
                 @BeanProperty md20: String,
                 @BeanProperty ma30: String
                )
