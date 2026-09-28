package zuk.sast.spring.controller.service

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.TushareConceptComponent
import zuk.tu_share.utils.Dataset_all_stocks_csv_file

import java.util
import scala.jdk.CollectionConverters.*

object Tushare_All_Impl {
  val all = "all"
  val all_desc = "全部"
}

@Service
class Tushare_All_Impl extends ITushareStockService {

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null

  override def getType(): String = Tushare_All_Impl.all

  override def getStocks(dto: QuerTushareStockDto): util.List[TushareStockControllerDTO] = {

    val allList = Dataset_all_stocks_csv_file.load.map(e=>{
      val dto = new TushareStockControllerDTO
      dto.selectModel = "全部"
      dto.stockCode = e.ts_code
      dto.name = e.name
      dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
      dto.eastmoneyURL = e.eastmoneyURL
      dto.conceptURL = e.conceptURL
      dto.attention = ""
      dto
    })

    //
    val list = if (StringUtils.isNotBlank(dto.desc)) {
      val splits = dto.desc.split("&").map(_.trim)
      allList.filter(e => {
        val size = splits.filter(s=>{
          e.stockCode.contains(s) || e.name.contains(s) || e.concept.contains(s)
        }).size
        size == splits.size
        //        e.stockCode.contains(desc) || e.name.contains(desc) || e.concept.contains(desc)
      })
    }
    else {
      allList
    }

    val num = 100
    val res = if (list.size > num) {
      list.take(num)
    }
    else {
      list
    }

    val attentionSet = super.getAllAttention()
    val buySet = super.getAllBuy()

    res.foreach(e => {

      if (attentionSet.contains(e.stockCode)) {
        e.attention = "已关注"
      }
      e.buy = ""
      if (buySet.contains(e.stockCode)) {
        e.buy = "已购买"
      }
      e
    })

    res.asJava

  }

}
