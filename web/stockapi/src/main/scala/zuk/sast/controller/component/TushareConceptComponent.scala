package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.controller.mapper.entity.StockInfoEntity
import zuk.sast.controller.mapper.{StockInfoMapper, StockMapper}
import zuk.token.TaskHandleFactory
import zuk.token.providers.deepseek.tasks.DeepseekTask_easymoneyConcept
import zuk.tu_share.dto.TsStock

import java.util.UUID
import java.util.concurrent.Executors
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
@Component
class TushareConceptComponent {

  @Autowired
  var stockMapper: StockMapper = null

  @Autowired
  var stockInfoMapper: StockInfoMapper = null

  @Autowired
  var tushareAllStocksCSVComponent: TushareAllStocksCSVComponent = null

  var executor = Executors.newSingleThreadExecutor()

  var cacheStockInfoList = new ListBuffer[StockInfoEntity]


  @PostConstruct
  def init(): Unit = {

    TaskHandleFactory.initTask()

    cacheStockInfoList ++= this.stockInfoMapper.selectAll().asScala

    this.stockMapper.selectAll().asScala.map(_.stockCode).toSet.foreach(stockCode=>{
      val stockName = tushareAllStocksCSVComponent.getTsStock(stockCode).getOrElse(new TsStock).name
      if(StringUtils.isNotEmpty(stockName) && !cacheStockInfoList.map(_.stockCode).toSet.contains(stockCode)){


        val tsStock = new TsStock
        tsStock.ts_code = stockCode
        val conceptURL = tsStock.getConceptURL()

        val task1 = new DeepseekTask_easymoneyConcept
        task1.stockCode = stockCode
        task1.stockName = stockName
        task1.stockConceptURL = conceptURL
        task1.chatContent = task1.createPrompt()
        //      println(s"创建Prompt提示词：${task1.chatContent}")
        TaskHandleFactory.TASK_QUEUE.push(task1)
      }
    })

    println(s"任务总数：${TaskHandleFactory.TASK_QUEUE.size()}")

    executor.execute(()=>{
      while (true){
        try {

          Thread.sleep(10 * 1000)

          if(TaskHandleFactory.TASK_QUEUE.size()>0){
            val firstTask = TaskHandleFactory.TASK_QUEUE.poll()
            if (firstTask != null) {
              firstTask match {
                case deepseekTask_easymoneyConcept: DeepseekTask_easymoneyConcept =>

                  var exist = false
                  val queueSize = TaskHandleFactory.TASK_QUEUE.size()
                  val answerSize = TaskHandleFactory.ANSWER_LIST.size
                  val answerList = TaskHandleFactory.ANSWER_LIST
                  answerList.foreach(e=>{
                    val tsStock = new TsStock
                    tsStock.ts_code = deepseekTask_easymoneyConcept.stockCode
                    tsStock.splitTsCode(tsStock.ts_code)
                    if(e.contains(tsStock.s_0) || e.contains(deepseekTask_easymoneyConcept.stockName)){
                      deepseekTask_easymoneyConcept.parserText = e
                      exist = true
                    }
                  })

                  if(exist){

                    val stockInfo = new StockInfoEntity
                    stockInfo.id = UUID.randomUUID().toString.replaceAll("-", "")
                    stockInfo.stockCode = deepseekTask_easymoneyConcept.stockCode
                    stockInfo.stockName = deepseekTask_easymoneyConcept.stockName
                    stockInfo.concept = firstTask.parserText
                    this.stockInfoMapper.insert(stockInfo)

                    cacheStockInfoList += stockInfo
                  }
                  else {
                    TaskHandleFactory.TASK_QUEUE.push(firstTask)
                  }


                case _ =>

              }
            }
          }

        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
        }
      }
    })
  }

}
