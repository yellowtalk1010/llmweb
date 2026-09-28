package zuk.sast.spring.controller.service

import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO

import java.util
import scala.jdk.CollectionConverters.*

object Tushare_Stock_limit_up_Impl {
  val limit_up = "limit_up"
  val limit_up_desc = "涨停"
}

@Service
class Tushare_Stock_limit_up_Impl extends TushareStock_limit_up_down with ITushareStockService {
  override def getType(): String = Tushare_Stock_limit_up_Impl.limit_up

  override def getStocks(dto: QuerTushareStockDto): util.List[TushareStockControllerDTO] = {
    super.getLimit_up_down(1, dto.selectedDateStart, dto.selectedDateEnd)
  }
}
