package zuk.tu_share.dto

import scala.beans.BeanProperty

/***
 * 龙虎榜机构交易单
 */
class TopInst {

  @BeanProperty var trade_date: String = _  //交易日期
  @BeanProperty var ts_code: String = _     //TS代码
  @BeanProperty var exalter: String = _     //营业部名称
  @BeanProperty var buy: String = _         //买入额（万）
  @BeanProperty var buy_rate: String = _    //买入占总成交比例
  @BeanProperty var sell: String = _        //卖出额（万）
  @BeanProperty var sell_rate: String = _   //卖出占总成交比例
  @BeanProperty var net_buy: String = _     //净成交额（万）
  @BeanProperty var side: String = _        //买卖类型0买入1卖出
  @BeanProperty var side_desc: String = _   //
  @BeanProperty var reason: String = _      //上榜理由

  @BeanProperty var count: Int = _          //数量


}
