package zuk.sast.controller.response

import scala.beans.BeanProperty

class JJQCData {

  @BeanProperty var mergeTime = 1 //合并次数

  @BeanProperty var jys: String = null //SZ，SH

  @BeanProperty var gl: String = null //行业分类


  //
  @BeanProperty var name: String = null //个股名称

  @BeanProperty var code: String = null //个股代码

  @BeanProperty var openAmt = .0 //开盘金额

  @BeanProperty var qczf = .0 //抢筹涨幅

  @BeanProperty var qccje = .0 //抢筹成交额

  @BeanProperty var qcwtje = .0 //抢筹委托金额

  @BeanProperty var `type`: Integer = null //

  @BeanProperty var period: Integer = null //

  @BeanProperty var time: String = null //


}
