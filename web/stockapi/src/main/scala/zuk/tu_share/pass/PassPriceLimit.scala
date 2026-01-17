package zuk.tu_share.pass
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.dto.ModuleDay

/***
 * 计算涨跌停的价格
 */
class PassPriceLimit extends IPass {

  override def handle(moduleDays: List[ModuleDay]): Unit = {
    moduleDays.filter(e=>StringUtils.isNotBlank(e.getPre_close)).foreach(e=>{
      val pl = e.priceLimit
      val preClose = e.getPre_close //上一个交易日的收盘价
      val tscode = e.getTs_code
    })
  }

  /***
   * 主板	  ±10%	新股上市首日最高涨44%，之后为±10%
   * 创业板	±20%	新股上市前5个交易日无涨跌幅，之后为±20%
   * 科创板	±20%	新股上市前5个交易日无涨跌幅，之后为±20%
   * 北交所	±30%	新股上市首日无涨跌幅，之后为±30%
   *
   */
  private def calPriceLimit(tucode: String, preClose: String) = {
    val splits = tucode.split("\\\\.")
    if(splits.size==2){
      if(splits(0).toUpperCase.equals("BJ")){
        //北交所  ±30%
      }
      else if(splits(1).startsWith("30")){
        //创业版 ±20%
      }
      else if(splits(1).startsWith("68")){
        //科创版 ±20%
      }
      else {
        //主板 ±10%
      }
    }
  }



}
