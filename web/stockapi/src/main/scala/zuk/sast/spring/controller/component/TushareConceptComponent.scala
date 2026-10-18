package zuk.sast.spring.controller.component

import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zuk.sast.spring.controller.mapper.{StockInfoMapper, StockMapper}
import zuk.sast.spring.controller.mapper.entity.{StockEntity, StockInfoEntity, StockInfoEntity as stockInfoEntity}
import zuk.token.TaskHandleFactory
import zuk.token.providers.tasks.Task_EasymoneyConcept
import zuk.tu_share.{DataFrame, ParseCammandParam}
import zuk.tu_share.dto.TsStock
import zuk.tu_share.utils.Dataset_all_stocks_csv_file

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

  var executor = Executors.newSingleThreadExecutor()

  var stockInfoEntityList = new ListBuffer[StockInfoEntity]

  def getStockConceptInfo(stockCode: String): String = synchronized {
    if(stockInfoEntityList.size == 0){
//      stockInfoEntityList ++= this.stockInfoMapper.selectAll().asScala
      load()
    }
    
    val ls = this.stockInfoEntityList.filter(_.stockCode.equals(stockCode))
    if(ls.size>0 && StringUtils.isNotEmpty(ls.head.concept)){
      val concept = ls.head.concept.split("\n").filter(l=>{
        l.contains("概念")
          || l.contains("一级行业")
          || l.contains("二级行业")
          || l.contains("三级行业")
      }).map(l=>s"【${l}】").mkString("")

      concept
    }
    else {
      ""
    }
  }


  /***
   * 根据股票代码构建md5作为唯一id
   *
   * @param input
   * @return
   */
  private def md5(input: String): String = {
    try {
      import java.security.MessageDigest
      import java.nio.charset.StandardCharsets

      val md = MessageDigest.getInstance("MD5")
      val digest = md.digest(input.getBytes(StandardCharsets.UTF_8))
      val sb = new java.lang.StringBuilder
      for (b <- digest) {
        sb.append(String.format("%02x", b & 0xff))
      }
      sb.toString
    } catch {
      case e: Exception =>
        throw new RuntimeException("MD5计算失败", e)
    }
  }
  
  private def load(): Unit = synchronized {
    //todo 初始化股票的 概念和板块 信息
    stockInfoEntityList ++=
      DataFrame.STOCKS_MAP.asScala.map(e => {
        val conceptFile = new File(ParseCammandParam.param.engineInfo.concept_dir + File.separator + e._2.ts_code + ".txt")
        println(s"加载【${e._2.ts_code}, ${e._2.name}】股票的概念板块路径：${conceptFile.getAbsolutePath}, ${conceptFile.exists()}")
        val concept = if (conceptFile.exists()) {
          FileUtils.readFileToString(conceptFile, "UTF-8")
        } else {
          ""
        }
        val stockInfoEntity = new StockInfoEntity()
        stockInfoEntity.stockCode = e._2.ts_code
        stockInfoEntity.stockName = e._2.name
        stockInfoEntity.concept = concept
        stockInfoEntity.id = md5(stockInfoEntity.stockCode)
        stockInfoEntity
      })

    this.stockInfoEntityList.toList.groupBy(_.stockCode).filter(_._2.size > 1).map(_._2).foreach(ls => {
      println(s"股票重复:${ls.head.stockCode}, ${ls.head.stockName}")
      System.exit(1) //重复的股票直接退出，人工处理
    })
  }

  @PostConstruct
  def init(): Unit = {

    load()
    
    //AI任务引擎初始化启动成功
    if(!TaskHandleFactory.initTask()){
      return
    }
    
    val list = this.stockInfoEntityList.filter(e=>StringUtils.isEmpty(e.concept))

    list.foreach(e=>{
      val tsStock = new TsStock(e.stockCode)

      val task = new Task_EasymoneyConcept
      task.id = e.id
      task.stockCode = e.stockCode
      task.stockName = e.stockName
      task.stockConceptURL = tsStock.conceptURL
      task.chatContent = task.createPrompt()
      TaskHandleFactory.TASK_QUEUE.push(task)
    })

    println(s"任务总数：${TaskHandleFactory.TASK_QUEUE.size()}")

    executor.execute(()=>{
      //启动
      var st = true
      while (st){
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
