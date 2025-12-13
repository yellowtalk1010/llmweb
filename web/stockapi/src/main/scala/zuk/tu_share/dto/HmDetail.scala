package zuk.tu_share.dto

import scala.beans.BeanProperty

class HmDetail {
  @BeanProperty var trade_date: String = _  //交易日期
  @BeanProperty var ts_code: String = _     //ts 代码
  @BeanProperty var ts_name: String = _     //股票名称
  @BeanProperty var buy_amount: String = _  //买入数量（万）
  @BeanProperty var sell_amount: String = _ //卖出数量（万）
  @BeanProperty var net_amount: String = _  //净买入（买卖和（万））
  @BeanProperty var hm_name: String = _     //游资名称
  @BeanProperty var hm_orgs: String = _     //关联机构
}
