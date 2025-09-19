package zuk.stockapi

import scala.beans.BeanProperty

class StockApiVo(@BeanProperty api_code: String,
                 @BeanProperty jys: String,
                 @BeanProperty name: String,
                 @BeanProperty gl: String)
