package zuk.sast.spring.controller.service.tushareStockImpl

import com.alibaba.fastjson2.JSONObject
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zuk.consolidation.{ConsolidationScanner, MainForceAccumulationScanner}
import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.TushareConceptComponent
import zuk.sast.spring.controller.service.tushareStockImpl.Tushare_consolidation_Impl
import zuk.tu_share.dto.TsStock

import java.io.File
import java.util
import java.util.concurrent.Executors
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object TushareStock_mainForceAccumulationScanner_Impl {
  val mainForceAccumulationScanner = "mainForceAccumulationScanner"
  val mainForceAccumulationScanner_desc = "主力吸筹"
}

@Service
class TushareStock_mainForceAccumulationScanner_Impl extends ITushareStockService{

  private val executor = Executors.newSingleThreadExecutor()
  
  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null

  @PostConstruct
  def init() = {
    executor.execute(new Runnable {
      override def run(): Unit = {
        println("预测吸筹")
        MainForceAccumulationScanner.main(Array.empty)
      }
    })
  }
  
  override def getType(): String = TushareStock_mainForceAccumulationScanner_Impl.mainForceAccumulationScanner

  override def getStocks(dto: QuerTushareStockDto): util.List[TushareStockControllerDTO] = {

    val dtoList = new ListBuffer[TushareStockControllerDTO]

    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()
    
    val lines = FileUtils.readLines(MainForceAccumulationScanner.ma10File, "UTF-8")

    dtoList ++= lines.asScala.map(l => {
      
      val jsonObj = JSONObject.parseObject(l)
      val stockCode = jsonObj.get("stockCode").asInstanceOf[String]
      val stockName = jsonObj.get("stockName").asInstanceOf[String]
      val tradedate = jsonObj.get("tradedate").asInstanceOf[String]
      
      val dto = new TushareStockControllerDTO
      dto.tradedate = tradedate
      dto.stockCode = stockCode
      dto.name = stockName
      dto.selectModel = "MA10_MODEL"
      dto.concept = ""
      val tsStock = new TsStock(stockCode, stockName)
      dto.eastmoneyURL = tsStock.eastmoneyURL
      dto.zjlxURL = tsStock.zjlxURL

      dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
      if (attentionSet.contains(dto.stockCode)) {
        dto.attention = "已关注"
      }
      if (buySet.contains(dto.stockCode)) {
        dto.buy = "已购买"
      }

      dto
    }).reverse

    dtoList.sortBy(_.tradedate).reverse.asJava
  }
  
  
}
