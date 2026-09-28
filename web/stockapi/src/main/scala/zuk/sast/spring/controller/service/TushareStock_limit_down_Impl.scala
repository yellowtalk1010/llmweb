package zuk.sast.spring.controller.service

import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO

import java.util
import scala.jdk.CollectionConverters.*

object _limit_down_Impl {
  val limit_down = "limit_down"
  val limit_down_desc = "跌停"
}

@Service
class _limit_down_Impl extends limit_up_down with ITushareStockService {
  override def getType(): String = _limit_down_Impl.limit_down

  override def getStocks(dto: QuerTushareStockDto): util.List[TushareStockControllerDTO] = {
    super.getLimit_up_down(-1, dto.selectedDateStart, dto.selectedDateEnd)
  }
}
