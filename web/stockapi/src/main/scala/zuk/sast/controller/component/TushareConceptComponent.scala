package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.controller.mapper.entity.StockInfoEntity
import zuk.sast.controller.mapper.{StockInfoMapper, StockMapper}
import zuk.token.TaskHandleFactory
import zuk.token.providers.deepseek.tasks.DeepseekTask_easymoneyConcept
import zuk.tu_share.dto.TsStock

import java.util.UUID
import java.util.concurrent.Executors
import scala.jdk.CollectionConverters.*
@Component
class TushareConceptComponent {

  @Autowired
  var stockMapper: StockMapper = null

  @Autowired
  var stockInfoMapper: StockInfoMapper = null

  var executor = Executors.newSingleThreadExecutor()

  @PostConstruct
  def init(): Unit = {

    TaskHandleFactory.initTask()

    this.stockMapper.selectAll().asScala.filter(_.stockType.equals("buy")).foreach(e=>{
      val stockCode = e.stockCode
      val stockName = e.name

      val tsStock = new TsStock
      tsStock.ts_code = stockCode
      val conceptURL = tsStock.getConceptURL()

      val task = new DeepseekTask_easymoneyConcept
      task.stockCode = stockCode
      task.stockName = stockName
      task.stockConceptURL = conceptURL
      task.chatContent = task.createPrompt()
      println(s"创建Prompt提示词：${task.chatContent}")
      TaskHandleFactory.TASK_QUEUE.push(task)
    })


    executor.execute(()=>{
      while (true){
        try {
          val task = TaskHandleFactory.TASK_QUEUE.peek()
          if(task!=null){
            if(task.checkResult()){
              val rmTask = TaskHandleFactory.TASK_QUEUE.pop()
              if(rmTask.checkResult()){
                rmTask match {
                  case deepseekTask_easymoneyConcept: DeepseekTask_easymoneyConcept =>
                    val stockInfo = new StockInfoEntity
                    stockInfo.id = UUID.randomUUID().toString.replaceAll("-", "")
                    stockInfo.stockCode = deepseekTask_easymoneyConcept.stockCode
                    stockInfo.stockName = deepseekTask_easymoneyConcept.stockName
                    stockInfo.concept = deepseekTask_easymoneyConcept.parserText
                    this.stockInfoMapper.insert(stockInfo)
                  case _=>
                }

              }
            }
          }
          Thread.sleep(5 * 1000)
        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
        }
      }
    })
  }

}
