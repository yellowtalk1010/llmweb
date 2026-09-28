package zuk.sast.spring.controller.service

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.{TushareConceptComponent, TushareInitMA4ModelMA5ModelComponent}
import zuk.tu_share.dto.TsStock
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.utils.{Dataset_top_Inst_dir, IncreateDecreateRateDescUtil}

import java.util
import scala.jdk.CollectionConverters.*

object TushareStock_module_Impl {
  val module = "module"
}

@Service
class TushareStock_module_Impl extends ITushareStockService {

  private val log = LoggerFactory.getLogger(classOf[TushareStock_module_Impl])

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null
  
  override def getType(): String = "module"

  override def getStocks(dto: QuerTushareStockDto): util.List[TushareStockControllerDTO] = {
    val ls = PassFactory.moduleList().map(_.getClass.getSimpleName.toUpperCase).filter(e => {
      e.equals(dto.status)
    })
    if (ls.size > 0) {
      this.getMa7(ls.head, dto.selectedDateStart, dto.selectedDateEnd)
    }
    else {
      new util.ArrayList[TushareStockControllerDTO]()
    }
  }

  private def getMa7(maStr: String, selectedDateStart: String, selectedDateEnd: String): java.util.List[TushareStockControllerDTO] = {
    log.info(s"getMa7, maStr:${maStr}, selectedDateStart:${selectedDateStart}, selectedDateEnd:${selectedDateEnd}")
    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

    //仅取前1000条记录
    println(s"TushareInitMA4ModelMA5ModelComponent.getStockEntityList：${TushareInitMA4ModelMA5ModelComponent.getStockEntityList.size}")
    val ls = TushareInitMA4ModelMA5ModelComponent.getStockEntityList.filter(_.stockType.equals(maStr))
    val list = if (ls.size > 1000) {
      ls.take(1000)
    }
    else {
      ls
    }

    val list1 = list.map(entity => {
      val dto = new TushareStockControllerDTO
      dto.selectModel = entity.stockType
      dto.stockCode = entity.stockCode
      dto.name = entity.name
      if (dto.stockCode.startsWith("688")) {
        dto.name = s"${dto.name}【科创】"
      }
      else if (dto.stockCode.startsWith("920")) {
        dto.name = s"${dto.name}【北交所】"
      }

      //计算历史出现的次数
      val totalList = list.filter(_.stockCode.equals(dto.stockCode)).sortBy(e => (e.createtime)).reverse
      val totalSize = totalList.size
      if (totalSize > 1) {
        dto.name = s"${dto.name}【历史总出现${totalSize}次最近${totalList.head.createtime}】"
      }

      //
      dto.tradedate = entity.createtime

      dto.topInstitutions = Dataset_top_Inst_dir.existTopInst(dto.stockCode)

      val optionTp3 = IncreateDecreateRateDescUtil.getDescription(dto.stockCode)

      dto.remark = optionTp3.get._4
      dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
      val tsStock = new TsStock(entity.stockCode)
      dto.eastmoneyURL = tsStock.eastmoneyURL
      dto.conceptURL = tsStock.conceptURL
      if (attentionSet.contains(dto.stockCode)) {
        dto.attention = "已关注"
      }
      dto.buy = ""
      if (buySet.contains(dto.stockCode)) {
        dto.buy = "已购买"
      }
      dto.createtime = entity.createtime
      //        Some(dto)
      dto
    })
    //      .filter(!_.isEmpty)
    //      .map(_.get)
    val list2 = list1.filter(e => {
        if (StringUtils.isNotBlank(selectedDateStart) && StringUtils.isNotBlank(selectedDateEnd)) {
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

          start <= e.createtime.trim.toLong && e.createtime.trim.toLong <= end
        }
        else if (StringUtils.isNotBlank(selectedDateStart)) {
          e.createtime.trim.equals(selectedDateStart.trim)
        }
        else if (StringUtils.isNotBlank(selectedDateEnd)) {
          e.createtime.trim.equals(selectedDateEnd.trim)
        }
        else {
          true
        }
      })
      .asJava

    if (list2.size() > 0) {
      log.info(s"时间范围:${list2.asScala.head.createtime}至${list2.asScala.last.createtime}")
    }

    list2
  }
  
}
