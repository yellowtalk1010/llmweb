package zuk.stockapi

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.io.FileUtils

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer

import scala.jdk.CollectionConverters.*

object CalculateMA {

  var MAP = new ConcurrentHashMap[String, List[StockMaVo]]

  def run(stocks: List[StockApiVo]): Unit = {
    MAP.clear()
    stocks.foreach(stock=>{
      try{
        calCode(stock)
      }
      catch
        case exception: Exception => exception.printStackTrace()
    })
  }

  private def calCode(stock: StockApiVo): Unit = {
    val code = stock.getApi_code
    val codeDataFile = new File(LoaderStockData.STOCK_DAY + File.separator + code)
    println( s"${codeDataFile.getAbsolutePath}, ${codeDataFile.exists()}, ${stock.getName}")
    val formatter = DateTimeFormatter.ofPattern("yyyyMM")
    val today = LocalDate.now
    val num = new AtomicInteger(0)
    val stockDayVoList = new ListBuffer[StockDayVo]
    for(i <- 0 until 3) {
      val premonthDate = today.minusMonths(i)
      val preMonth = formatter.format(premonthDate)
      val path = LoaderStockData.STOCK_DAY + File.separator + code + File.separator + preMonth + ".jsonl"
      val file = new File(path)
      if (!file.exists) {
        println(path + "，不存在")
      }
      else {
        try {
          //读取文件中的数据
          val lines = FileUtils.readLines(file, "UTF-8")
          //将文件行数据转成json对象
          val ls: List[StockDayVo] = lines.asScala.map((line: String) => {
            val stockDayVo = JSONObject.parseObject(line, classOf[StockDayVo])
            stockDayVo
          }).toList
          stockDayVoList ++= ls
        }
        catch
          case exception: Exception => exception.printStackTrace()
      }
    }
    //按时间降序
    val sorted = stockDayVoList.sortBy(_.getTime).reverse
    //println(sorted.size)



  }

}

