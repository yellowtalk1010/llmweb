package zuk.sast.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.controller.mapper.entity.{StockEntity, StockInfoEntity}
import zuk.sast.controller.mapper.{StockInfoMapper, StockMapper}
import zuk.token.TaskHandleFactory
import zuk.token.providers.tasks.Task_EasymoneyConcept
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

  var stockInfoEntityList = new ListBuffer[StockInfoEntity]

  def getStockConceptInfo(stockCode: String): String = synchronized {
    if(stockInfoEntityList.size==0){
      stockInfoEntityList ++= this.stockInfoMapper.selectAll().asScala
    }
    val ls = this.stockInfoEntityList.filter(_.stockCode.equals(stockCode))
    if(ls.size>0 && StringUtils.isNotEmpty(ls.head.concept)){
      val concept = ls.head.concept.split("\n").filter(l=>{
        l.trim.startsWith("概念")
          || l.startsWith("一级行业")
          || l.startsWith("二级行业")
          || l.startsWith("三级行业")
      }).map(l=>s"【${l}】").mkString("")

      concept
    }
    else {
      ""
    }
  }

  /***
   * 获取待处理的股票
   * @return
   */
  private def getHandleTaskStockList(): Unit = synchronized {

    //todo 删除stock_info表中重复的数据
    this.stockInfoMapper.selectAll().asScala.groupBy(_.stockCode).filter(_._2.size > 1).map(_._2).foreach(ls => {
      for (i <- 1 until ls.size) {
        this.stockInfoMapper.deleteById(ls(i).id)
      }
    })


    val cacheStockInfoList = this.stockInfoMapper.selectAll().asScala

    //todo 获取ai提取概念和板块的热点股票
    val lls = this.stockMapper.selectAll().asScala.groupBy(_.stockCode).map(e => {
      val ls = e._2.sortBy(_.createtime).reverse
      ls.head
    }).toList.sortBy(_.createtime).reverse.filter(e => {
      !cacheStockInfoList.map(_.stockCode).toSet.contains(e.stockCode)
    })

    lls.foreach(se=>{
      val stockInfoEntity = new StockInfoEntity
      stockInfoEntity.id = UUID.randomUUID().toString.replaceAll("-", "")
      stockInfoEntity.stockCode = se.stockCode
      stockInfoEntity.stockName = se.name
      this.stockInfoMapper.insert(stockInfoEntity)
    })



  }

  @PostConstruct
  def init(): Unit = {

    if(!TaskHandleFactory.initTask()){
      return
    }
    getHandleTaskStockList()

    val list = this.stockInfoMapper.selectAll().asScala.filter(e=>StringUtils.isEmpty(e.concept))

    list.foreach(e=>{
      val tsStock = new TsStock
      tsStock.ts_code = e.stockCode
      val conceptURL = tsStock.getConceptURL()

      val task = new Task_EasymoneyConcept
      task.id = e.id
      task.stockCode = e.stockCode
      task.stockName = e.stockName
      task.stockConceptURL = conceptURL
      task.chatContent = task.createPrompt()
      TaskHandleFactory.TASK_QUEUE.push(task)
    })

    println(s"任务总数：${TaskHandleFactory.TASK_QUEUE.size()}")

    executor.execute(()=>{
      //启动
      var st = true
      while (st && false){
        try {

          val taskResultDir = new File("task_ai")
          if(taskResultDir.exists() && taskResultDir.isDirectory){
            //获取全部结果文件
            val files = taskResultDir.listFiles()
            val filecontextList = files.map(f=>{
              val lines = FileUtils.readLines(f, "UTF-8")
              val str = lines.asScala.mkString("\n")
              (f, str)
            })

            val delFiles = new ListBuffer[File]

            //任务与结果进行匹配
            list.foreach(entity => {
              val id = entity.id
              filecontextList.filter(tp2=>tp2._2.contains(id)).foreach(tp2=>{
                entity.concept = tp2._2
                this.stockInfoMapper.updateConceptById(tp2._2, id)
                println(s"删除${entity.stockCode}, ${entity.stockName}, ${tp2._1.getAbsolutePath}")
                delFiles += tp2._1
              })
            })
            //删除结果
            delFiles.foreach(f=>{
              println(s"删除结果文件:${f.getAbsolutePath}")
              f.delete()
            })
          }
          else {
            st = false
          }

          Thread.sleep(5 * 60 * 1000)
        }
        catch {
          case exception: Exception =>
            exception.printStackTrace()
        }
      }
    })
  }

}
