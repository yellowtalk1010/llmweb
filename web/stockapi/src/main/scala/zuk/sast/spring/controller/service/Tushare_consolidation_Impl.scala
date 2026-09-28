package zuk.sast.spring.controller.service

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.TushareConceptComponent
import zuk.tu_share.dto.TsStock

import java.io.File
import java.util
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object Tushare_consolidation_Impl {
  val consolidation = "consolidation"
  val consolidation_desc = "黏合预测"
}

@Service
class Tushare_consolidation_Impl extends ITushareStockService {
  
  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null
  
  override def getType(): String = Tushare_consolidation_Impl.consolidation

  override def getStocks(dto: QuerTushareStockDto): util.List[TushareStockControllerDTO] = {

    val dtoList = new ListBuffer[TushareStockControllerDTO]

    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

    val exampleFile = new File("stockapi/target/example.txt")
    println(s"样例：${exampleFile.getAbsolutePath}, ${exampleFile.exists()}")
    if (exampleFile.exists()) {
      val lines = FileUtils.readLines(exampleFile, "UTF-8")
      dtoList ++= lines.asScala.map(l => {
        val array = l.split(" ").filter(e => StringUtils.isNotBlank(e.trim))
        val stockCode = array(0)
        val stockName = array(1)
        val remark = array(2)
        val dto = new TushareStockControllerDTO
        dto.stockCode = stockCode
        dto.name = stockName
        dto.remark = remark
        dto.selectModel = "样例"
        dto.concept = ""
        dto.eastmoneyURL = new TsStock(stockCode, stockName).eastmoneyURL
        dto
      })
    }

    val futureFile = new File("stockapi/target/future.txt")
    println(s"预测：${futureFile.getAbsolutePath}， ${futureFile.exists()}")
    if (futureFile.exists()) {
      val lines = FileUtils.readLines(futureFile, "UTF-8")
      dtoList ++= lines.asScala.map(l => {
        val array = l.split(" ").filter(e => StringUtils.isNotBlank(e.trim))
        val stockCode = array(0)
        val stockName = array(1)
        val remark = array(2)
        val dto = new TushareStockControllerDTO
        dto.stockCode = stockCode
        dto.name = stockName
        dto.remark = remark
        dto.selectModel = "预测"
        dto.concept = ""
        dto.eastmoneyURL = new TsStock(stockCode, stockName).eastmoneyURL

        dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
        if (attentionSet.contains(dto.stockCode)) {
          dto.attention = "已关注"
        }
        if (buySet.contains(dto.stockCode)) {
          dto.buy = "已购买"
        }

        dto
      })
    }


    dtoList.asJava
  }

}
