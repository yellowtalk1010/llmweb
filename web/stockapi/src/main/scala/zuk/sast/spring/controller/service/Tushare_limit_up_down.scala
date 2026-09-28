package zuk.sast.spring.controller.service

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.TushareConceptComponent
import zuk.tu_share.DataFrame
import zuk.tu_share.dto.TsStock
import zuk.tu_share.utils.Dataset_all_stocks_csv_file

import scala.jdk.CollectionConverters.*

@Service
abstract class Tushare_limit_up_down {

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null

  /** *
   * 涨停、跌停的股票
   *
   * @param up_down_type 1,涨停，-1跌停
   * @param selectedDateStart
   * @param selectedDateEnd
   * @return
   */
  def getLimit_up_down(up_down_type: Int = 1, selectedDateStart: String, selectedDateEnd: String): java.util.List[TushareStockControllerDTO] = {
    val list = Dataset_all_stocks_csv_file.load.map(_.ts_code)
      .filter(e => {
        val list = DataFrame.getDataForSelect(e)
        list != null && list.size > 0
      })
      .flatMap(stockCode => {
        val ls = DataFrame.getDataForSelect(stockCode)
        //开始过滤时间
        val filterList = if (StringUtils.isNotBlank(selectedDateStart) && StringUtils.isNotBlank(selectedDateEnd)) {
          val start = if (selectedDateStart.trim.toLong <= selectedDateEnd.trim.toLong) {
            selectedDateStart.trim.toLong
          }
          else {
            selectedDateEnd.trim.toLong
          }

          val end = if (selectedDateStart.trim.toLong <= selectedDateEnd.trim.toLong) {
            selectedDateEnd.trim.toLong
          }
          else {
            selectedDateStart.trim.toLong
          }

          ls.sortBy(e => e.trade_date.toFloat).reverse.filter(e => start <= e.trade_date.toLong && e.trade_date.toLong <= end)

        }
        else if (StringUtils.isNotBlank(selectedDateStart)) {
          ls.filter(e => e.trade_date.equals(selectedDateStart))
        }
        else if (StringUtils.isNotBlank(selectedDateEnd)) {
          ls.filter(e => e.trade_date.equals(selectedDateEnd))
        }
        else {
          if (ls.size > 0) {
            Array(ls.head).toList
          }
          else {
            List.empty
          }

        }
        filterList
      }).filter(e => {
        if (up_down_type == 1) {
          e.change.toFloat > 9.8
        }
        else {
          e.change.toFloat < -9.8
        }
      })

    //
    list.map(e => {
      val dto = new TushareStockControllerDTO
      dto.selectModel = if (up_down_type == 1) {
        "涨停"
      }
      else {
        "跌停"
      }
      dto.tradedate = e.trade_date
      dto.stockCode = e.ts_code
      dto.name = e.name
      if (dto.stockCode.startsWith("688")) {
        dto.name = s"${dto.name}【科创】"
      }
      else if (dto.stockCode.startsWith("920")) {
        dto.name = s"${dto.name}【北交所】"
      }
      dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
      val tsStock = new TsStock(dto.stockCode)
      dto.eastmoneyURL = tsStock.eastmoneyURL
      dto.conceptURL = tsStock.conceptURL
      dto
    }).asJava

  }
  
}
