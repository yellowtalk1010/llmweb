package zuk.tu_share.pass
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.dto.ModuleDay
import java.math.BigDecimal
import zuk.tu_share.dto.PriceLimit

/***
 * 计算涨跌停的价格
 */
class PassPriceLimit extends IPass {

  override def handle(moduleDays: List[ModuleDay]): Unit = {
    moduleDays.filter(e=>StringUtils.isNotBlank(e.getPre_close)).foreach(e=>{
      val pl = e.priceLimit
      val preClose = e.getPre_close //上一个交易日的收盘价
      val tscode = e.getTs_code

      val tp2 = calPriceLimit(tscode, preClose)
      if(tp2!=null){
        val priceLimit = new PriceLimit()
        priceLimit.setPriceLimitUp(tp2._1)
        priceLimit.setPriceLimitDown(tp2._2)
      }
    })
  }

  /***
   * 主板	  ±10%	新股上市首日最高涨44%，之后为±10%
   * 创业板	±20%	新股上市前5个交易日无涨跌幅，之后为±20%
   * 科创板	±20%	新股上市前5个交易日无涨跌幅，之后为±20%
   * 北交所	±30%	新股上市首日无涨跌幅，之后为±30%
   *
   */
  private def calPriceLimit(tucode: String, preClose: String): Tuple2[BigDecimal, BigDecimal] = {
    val splits = tucode.split("\\\\.")
    if(splits.size==2){
      if(splits(0).toUpperCase.equals("BJ")){
        //北交所  ±30%
        val upRate = new BigDecimal(1).add(new BigDecimal(0.3))
        val downRate = new BigDecimal(1).subtract(new BigDecimal(0.3))

        val priceLimitUp = new BigDecimal(preClose).multiply(upRate)
        val priceLimitDown = new BigDecimal(preClose).multiply(downRate)

        return (priceLimitUp, priceLimitDown)

      }
      else if(splits(1).startsWith("30")){
        //创业版 ±20%

        val upRate = new BigDecimal(1).add(new BigDecimal(0.2))
        val downRate = new BigDecimal(1).subtract(new BigDecimal(0.2))

        val priceLimitUp = new BigDecimal(preClose).multiply(upRate)
        val priceLimitDown = new BigDecimal(preClose).multiply(downRate)

        return (priceLimitUp, priceLimitDown)
      }
      else if(splits(1).startsWith("68")){
        //科创版 ±20%

        val upRate = new BigDecimal(1).add(new BigDecimal(0.2))
        val downRate = new BigDecimal(1).subtract(new BigDecimal(0.2))

        val priceLimitUp = new BigDecimal(preClose).multiply(upRate)
        val priceLimitDown = new BigDecimal(preClose).multiply(downRate)

        return (priceLimitUp, priceLimitDown)
      }
      else {
        //主板 ±10%

        val upRate = new BigDecimal(1).add(new BigDecimal(0.1))
        val downRate = new BigDecimal(1).subtract(new BigDecimal(0.1))

        val priceLimitUp = new BigDecimal(preClose).multiply(upRate)
        val priceLimitDown = new BigDecimal(preClose).multiply(downRate)


        return (priceLimitUp, priceLimitDown)
      }
    }

    null
  }



}
