package zuk.sast.spring.controller.service

import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO

import java.util

import scala.jdk.CollectionConverters.*

object Tushare_All_Impl {
  val ALL = "all"
  val ALL_DESC = "全部"
}

@Service
class Tushare_All_Impl extends ITushareStockService {
  
  override def getType(): String = Tushare_All_Impl.ALL

  override def getStocks(): util.List[TushareStockControllerDTO] = {
    List.empty.asJava
  }
  
}
