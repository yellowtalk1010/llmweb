package zuk.sast.spring.controller.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.{TushareConceptComponent, TushareInitMA4ModelMA5ModelComponent}
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, Dataset_top_Inst_dir, IncreateDecreateRateDescUtil}

import java.util
import scala.jdk.CollectionConverters.*

object TushareStock_My_Impl {
  val MY = "my"
  val MY_DESC = "我的"
}

@Service
class TushareStock_My_Impl extends ITushareStockService {

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null
  
  override def getType(): String = TushareStock_My_Impl.MY

  override def getStocks(): util.List[TushareStockControllerDTO] = {

    //购买的股票
    val buySet = super.getAllBuy()
    //关注的股票
    val attentionSet = super.getAllAttention()
    val sets = buySet ++ attentionSet

    //ma4次数
    val ma5List = TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(TushareInitMA4ModelMA5ModelComponent.MA5_MODEL_STR))

    val tsStockList = sets.toList.map(e => {
        Dataset_all_stocks_csv_file.getTsStock(e)
      }).filter(!_.isEmpty)
      .map(e => {
        val dto = new TushareStockControllerDTO
        dto.selectModel = "我的"
        dto.stockCode = e.get.ts_code
        val codeList = ma5List.filter(_.stockCode.equals(e.get.ts_code)).sortBy(_.createtime).reverse
        dto.name = if (codeList.size == 0) e.get.name else s"${e.get.name}【${codeList.size}次】${codeList.head.createtime}"
        if (dto.stockCode.startsWith("688")) {
          dto.name = s"${dto.name}【科创】"
        }
        else if (dto.stockCode.startsWith("920")) {
          dto.name = s"${dto.name}【北交所】"
        }

        //龙虎榜
        dto.topInstitutions = Dataset_top_Inst_dir.existTopInst(dto.stockCode)

        val optionTp3 = IncreateDecreateRateDescUtil.getDescription(dto.stockCode)
        dto.remark = optionTp3.get._4
        dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
        dto.eastmoneyURL = e.get.eastmoneyURL
        dto.conceptURL = e.get.conceptURL
        if (attentionSet.contains(dto.stockCode)) {
          dto.attention = "已关注"
        }
        if (buySet.contains(dto.stockCode)) {
          dto.buy = "已购买"
        }
        dto
      }).sortBy(e => (e.buy, e.stockCode)).reverse.asJava

    tsStockList
    
  }


  
}
