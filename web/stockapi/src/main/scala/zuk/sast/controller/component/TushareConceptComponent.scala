package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.controller.mapper.entity.{StockEntity, StockInfoEntity}
import zuk.sast.controller.mapper.{StockInfoMapper, StockMapper}
import zuk.token.TaskHandleFactory
import zuk.token.providers.deepseek.tasks.DeepseekTask_easymoneyConcept
import zuk.tu_share.dto.TsStock

import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

/***
 * ai分析股票的概念和板块
 */
@Component
class TushareConceptComponent {

  @Autowired
  var stockMapper: StockMapper = null

  @Autowired
  var stockInfoMapper: StockInfoMapper = null

  @Autowired
  var tushareAllStocksCSVComponent: TushareAllStocksCSVComponent = null

  var executor = Executors.newSingleThreadExecutor()


  /***
   * 获取待处理的股票
   * @return
   */
  private def getHandleTaskStockList(): List[StockEntity] = {

    //todo 删除重复的数据
    this.stockInfoMapper.selectAll().asScala.groupBy(_.stockCode).filter(_._2.size > 1).map(_._2).foreach(ls => {
      for (i <- 1 until ls.size) {
        this.stockInfoMapper.deleteById(ls(i).id)
      }
    })

    val cacheStockInfoList = new ListBuffer[StockInfoEntity]
    cacheStockInfoList ++= this.stockInfoMapper.selectAll().asScala

    //todo 获取ai提取概念和板块的热点股票
    val lls = this.stockMapper.selectAll().asScala.groupBy(_.stockCode).map(e => {
      val ls = e._2.sortBy(_.createtime).reverse
      ls.head
    }).toList.sortBy(_.createtime).reverse.filter(e => {
      !cacheStockInfoList.map(_.stockCode).toSet.contains(e.stockCode)
    })

    lls

  }

  @PostConstruct
  def init(): Unit = {

    TaskHandleFactory.initTask()



//    lls.take(500).map(_.stockCode).foreach(stockCode=>{
//      val stockName = tushareAllStocksCSVComponent.getTsStock(stockCode).getOrElse(new TsStock).name
//      if(StringUtils.isNotEmpty(stockName) && !cacheStockInfoList.map(_.stockCode).toSet.contains(stockCode)){
//
//
//        val tsStock = new TsStock
//        tsStock.ts_code = stockCode
//        val conceptURL = tsStock.getConceptURL()
//
//        val task1 = new DeepseekTask_easymoneyConcept
//        task1.stockCode = stockCode
//        task1.stockName = stockName
//        task1.stockConceptURL = conceptURL
//        task1.chatContent = task1.createPrompt()
//        //      println(s"创建Prompt提示词：${task1.chatContent}")
////        TaskHandleFactory.TASK_QUEUE.push(task1)
//      }
//    })

    println(s"任务总数：${TaskHandleFactory.TASK_QUEUE.size()}")

    executor.execute(()=>{
      //启动
      while (false){
        try {

          Thread.sleep(30 * 1000)

          if(TaskHandleFactory.TASK_QUEUE.size() < 50){
            val lls = getHandleTaskStockList()
            if(lls.size>50){
              lls.take(50).foreach(e=>{
                  //todo 将待处理的股票转成任务
                  val tsStock = new TsStock
                  tsStock.ts_code = e.stockCode
                  val conceptURL = tsStock.getConceptURL()

                  val task1 = new DeepseekTask_easymoneyConcept
                  task1.stockCode = e.stockCode
                  task1.stockName = e.name
                  task1.stockConceptURL = conceptURL
                  task1.chatContent = task1.createPrompt()
                  //      println(s"创建Prompt提示词：${task1.chatContent}")
                  TaskHandleFactory.TASK_QUEUE.push(task1)
              })
            }
          }

          
          val lls = getHandleTaskStockList()

          val taskResDir = new File("task_ai")
          if(taskResDir.exists()){
            taskResDir.listFiles().foreach(f=>{
              val taskResStr = FileUtils.readLines(f, "utf-8").asScala.mkString("\n")
              val hits = lls.filter(e=>{
                val tsStock = new TsStock
                tsStock.ts_code = e.stockCode
                tsStock.splitTsCode(tsStock.ts_code)
                taskResStr.contains(tsStock.s_0)
              })
              if(hits.size==1){

                val stockInfo = new StockInfoEntity
                stockInfo.id = UUID.randomUUID().toString.replaceAll("-", "")
                stockInfo.stockCode = hits.head.stockCode
                stockInfo.stockName = hits.head.name
                stockInfo.concept = taskResStr
                //this.stockInfoMapper.insert(stockInfo)

              }
              else if (hits.size==0) {
                //
              }
              else {
                println()
              }
            })
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
