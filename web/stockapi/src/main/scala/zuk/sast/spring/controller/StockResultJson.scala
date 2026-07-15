package zuk.sast.spring.controller

import java.io.File
import scala.beans.BeanProperty

case class StockResultJson(){

  @BeanProperty var file: File = null
  @BeanProperty var fileName: String = ""
  @BeanProperty var eastmoneyURL: String = "" //东方财富地址
  @BeanProperty var conceptURL: String = "" //东方财务概念地址

  @BeanProperty var area: String = ""
  @BeanProperty var modDesc: String = ""
  @BeanProperty var ts_code: String = ""
  @BeanProperty var turnoverRate: String = ""
  @BeanProperty var name: String = ""

  @BeanProperty var limitUp: String = ""
  @BeanProperty var industry: String = ""
  @BeanProperty var limitDown: String = ""
  @BeanProperty var modWinRate: String = ""
  @BeanProperty var modClsName: String = ""

  @BeanProperty var upperShadow: String = "" //上影线警告

  @BeanProperty var attention: String = "" //是否关注
  @BeanProperty var buy: String = "" //是否购买
  @BeanProperty var eliminate: String = "" //是否淘汰

  @BeanProperty var remark: String = "" //备注
  @BeanProperty var concept: String = ""//股票的概念和板块

}

