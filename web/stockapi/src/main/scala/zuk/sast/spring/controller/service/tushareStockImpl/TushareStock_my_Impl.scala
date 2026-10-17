package zuk.sast.spring.controller.service.tushareStockImpl

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.{TushareConceptComponent, TushareInitMA4ModelMA5ModelComponent}
import zuk.sast.spring.controller.service.tushareStockImpl.TushareStock_My_Impl
import zuk.tu_share.utils.{Dataset_all_stocks_csv_file, Dataset_top_Inst_dir, IncreateDecreateRateDescUtil}

import java.util
import scala.jdk.CollectionConverters.*

object TushareStock_My_Impl {
  val my = "my"
  val my_desc = "我的"
}

@Service
class TushareStock_My_Impl extends ITushareStockService {

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null

  override def getType(): String = TushareStock_My_Impl.my

  override def getStocks(dto: QuerTushareStockDto = null): util.List[TushareStockControllerDTO] = {

    //购买的股票
    val buySet = super.getAllBuy()
    //关注的股票
    val attentionSet = super.getAllAttention()
    val sets = buySet ++ attentionSet

    val tsStockList = sets.toList.map(e => {
        Dataset_all_stocks_csv_file.getTsStock(e)
      }).filter(!_.isEmpty)
      .map(e => {
        val dto = new TushareStockControllerDTO
        dto.selectModel = "我的"
        dto.stockCode = e.get.ts_code
        dto.name = e.get.name

        //todo 计算改股票在各模型中出现的次数
        val str = TushareStock_module_Impl.modelTestbackResultMap.asScala.map(tp2 => {
          val times = tp2._2.filter(modTestbackRes=>modTestbackRes.stockCode.equals(e.get.ts_code)).size
          if(times==0){
            ""
          }
          else {
            s"${tp2._1}模型${times}次"  
          }
        }).filter(StringUtils.isNotBlank(_)).mkString(";").replaceAll("_MODEL", "")
        if(StringUtils.isNotBlank(str)){
          dto.name = s"${dto.name}【${str}】"
        }
        
        //todo 区分股票类型
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
        dto.zjlxURL = e.get.zjlxURL
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
