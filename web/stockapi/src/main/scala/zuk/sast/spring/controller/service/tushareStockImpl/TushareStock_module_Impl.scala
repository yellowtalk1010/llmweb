package zuk.sast.spring.controller.service.tushareStockImpl

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zuk.sast.spring.controller.TushareStockControllerDTO
import zuk.sast.spring.controller.component.TushareConceptComponent
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.TsStock
import zuk.tu_share.pass.PassFactory
import zuk.tu_share.utils.{Dataset_top_Inst_dir, IncreateDecreateRateDescUtil}

import java.io.File
import java.util
import java.util.concurrent.ConcurrentHashMap
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

/***
 * 模型回测结果集
 */
case class ModelTestbackResultDto() {
  @BeanProperty var stockCode: String = ""
  @BeanProperty var stockName: String = ""
  @BeanProperty var stockType: String = ""
  @BeanProperty var tradedate: String = ""
}

object TushareStock_module_Impl {

  val module = "module"
  val module_names = PassFactory.engineModuleList().map(_.getClass.getSimpleName.toUpperCase)
  var modelTestbackResultMap = new ConcurrentHashMap[String, List[ModelTestbackResultDto]]()

  load()

  /***
   * 加载模型回测结果数据集
   */
  private def load(): Unit = synchronized {
    if(modelTestbackResultMap.size() > 0){
      return
    }
    val dir = new File(ParseCammandParam.param.engineInfo.MODEL_BACK_TEST_RESULT_dir)
    println(s"加载回测结果集路径:${dir.getAbsolutePath}, ${dir.exists()}")
    dir.listFiles().map(f=>{
      val fileName = f.getName
      println(s"加载${fileName}模型的数据集:${f.getAbsolutePath}")
      val dtos = FileUtils.readLines(f, "UTF-8").asScala.filter(l=>StringUtils.isNotBlank(l)).map(l=>{
        val dto = JSONObject.parseObject(l, classOf[ModelTestbackResultDto])
        dto
      })
      dtos
    }).foreach(ls=>{
      modelTestbackResultMap.put(ls.head.stockType, ls.toList)
    })
  }
}


@Service
class TushareStock_module_Impl extends ITushareStockService {

  private val log = LoggerFactory.getLogger(classOf[TushareStock_module_Impl])

  @Autowired
  private var tushareConceptComponent: TushareConceptComponent = null
  
  override def getType(): String = "module"

  override def getStocks(dto: QuerTushareStockDto): util.List[TushareStockControllerDTO] = {
    val ls = PassFactory.engineModuleList().map(_.getClass.getSimpleName.toUpperCase).filter(e => {
      e.equals(dto.status)
    })
    if (ls.size > 0) {
      this.getMa7(ls.head, dto.desc, dto.selectedDateStart, dto.selectedDateEnd)
    }
    else {
      new util.ArrayList[TushareStockControllerDTO]()
    }
  }

  private def getMa7(maStr: String, desc: String, selectedDateStart: String, selectedDateEnd: String): java.util.List[TushareStockControllerDTO] = {
    log.info(s"getMa7, maStr:${maStr}, desc:${desc}, selectedDateStart:${selectedDateStart}, selectedDateEnd:${selectedDateEnd}")
    //购买的股票
    val buySet = getAllBuy()
    //关注的股票
    val attentionSet = getAllAttention()

    //仅取前1000条记录
    val ls = TushareStock_module_Impl.modelTestbackResultMap.get(maStr.toUpperCase).filter(dto=>{
      StringUtils.isEmpty(desc) || dto.stockCode.contains(desc) || dto.stockName.contains(desc) 
    })
    val list = if (ls.size > 1000) {
      ls.take(1000)
    }
    else {
      ls
    }

    val list1 = list.map(modTBResDto => {
      val dto = new TushareStockControllerDTO
      dto.selectModel = modTBResDto.stockType
      dto.stockCode = modTBResDto.stockCode
      dto.name = modTBResDto.stockName
      if (dto.stockCode.startsWith("688")) {
        dto.name = s"${dto.name}【科创】"
      }
      else if (dto.stockCode.startsWith("920")) {
        dto.name = s"${dto.name}【北交所】"
      }

      //计算历史出现的次数
      val totalList = ls.filter(_.stockCode.equals(dto.stockCode)).filter(_.tradedate.toLong < modTBResDto.tradedate.toLong).sortBy(e => e.tradedate).reverse
      val totalSize = totalList.size
      if (totalSize > 1) {
        dto.name = s"${dto.name}【历史总出现${totalSize}次最近${totalList.head.tradedate}】"
      }

      //
      dto.tradedate = modTBResDto.tradedate

      dto.topInstitutions = Dataset_top_Inst_dir.existTopInst(dto.stockCode)

      val optionTp3 = IncreateDecreateRateDescUtil.getDescription(dto.stockCode)

      dto.remark = optionTp3.get._4
      dto.concept = this.tushareConceptComponent.getStockConceptInfo(dto.stockCode)
      val tsStock = new TsStock(modTBResDto.stockCode)
      dto.eastmoneyURL = tsStock.eastmoneyURL
      dto.conceptURL = tsStock.conceptURL
      dto.zjlxURL = tsStock.zjlxURL
      if (attentionSet.contains(dto.stockCode)) {
        dto.attention = "已关注"
      }
      dto.buy = ""
      if (buySet.contains(dto.stockCode)) {
        dto.buy = "已购买"
      }
      dto.createtime = modTBResDto.tradedate
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
