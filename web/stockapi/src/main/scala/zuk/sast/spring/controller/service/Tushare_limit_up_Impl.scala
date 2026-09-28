package zuk.sast.spring.controller.service

import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO

import java.util

object Tushare_limit_up_Impl {
  val limit_up = "limit_up"
  val limit_up_desc = "涨停"
}

@Service
class Tushare_limit_up_Impl extends ITushareStockService {
  override def getType(): String = 

  override def getStocks(dto: QuerTushareStockDto): util.List[TushareStockControllerDTO] = ???
}
