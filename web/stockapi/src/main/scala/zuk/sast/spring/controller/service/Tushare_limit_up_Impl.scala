package zuk.sast.spring.controller.service

import org.springframework.stereotype.Service

object Tushare_limit_up_Impl {
  val limit_up = "limit_up"
  val limit_up_desc = "涨停"
}

@Service
class Tushare_limit_up_Impl extends ITushareStockService {

}
