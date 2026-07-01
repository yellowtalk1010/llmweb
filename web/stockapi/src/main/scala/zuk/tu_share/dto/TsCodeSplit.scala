package zuk.tu_share.dto

import org.apache.commons.lang3.StringUtils

import scala.beans.BeanProperty

trait TsCodeSplit {

  @BeanProperty var s_0: String = "" //000001.SZ中的000001
  @BeanProperty var s_1: String = "" //000001.SZ中的SZ

  /***
   * 将ts_code拆分为000001和SZ
   */
  def splitTsCode(ts_code: String): Unit = {
    if(StringUtils.isNotBlank(ts_code)){
      val splits = ts_code.split("\\.")
      if (splits.size == 2) {
        s_0 = splits(0)
        s_1 = splits(1)
      }
    }
  }

}
