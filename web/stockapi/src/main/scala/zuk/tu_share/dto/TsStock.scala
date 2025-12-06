package zuk.tu_share.dto

import scala.beans.BeanProperty

class TsStock {

  @BeanProperty var ts_code: String = _     //ts代码
  @BeanProperty var symbol: String = _      //股票代码
  @BeanProperty var name: String = _        //名称
  @BeanProperty var area: String = _        //地域
  @BeanProperty var industry: String = _    //所属行业
  @BeanProperty var market: String = _      //市场类型（主板/创业板/科创板/CDR）

}

