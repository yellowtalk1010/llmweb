package zuk.stockapi

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.io.FileUtils

import java.io.File
import java.util
import java.util.{ArrayList, List}
import scala.beans.BeanProperty

object LoaderStockData {

  val TOKEN = "36c92182f783f08005017f78e7a264608a82952f8b91de2a"

  val STOCK_DATA_DIR_PATH = "D:\\AAAAAAAAAAAAAAAAAAAA\\github\\llmweb1\\web\\stocks"
  val STOCK_TOKEN = STOCK_DATA_DIR_PATH + File.separator + "token"
  val STOCK_ALL = STOCK_DATA_DIR_PATH + File.separator + "all_stock.json"
  val STOCK_DAY = STOCK_DATA_DIR_PATH + File.separator + "days"
  val STOCK_MA = STOCK_DATA_DIR_PATH + File.separator + "ma"

  val STOCKS: util.List[StockApiVo] = new util.ArrayList[StockApiVo]


  private def loadAllStocks(): Unit = {
    try {
      val file = new File(STOCK_ALL)
      if(file!=null && file.exists() && file.isFile){
        //TODO
      }
      else {
        println(s"${STOCK_ALL}不存在，退出")
        System.exit(1)
      }

      val content = FileUtils.readFileToString(file, "UTF-8")
      val jsonObject = JSONObject.parseObject(content)
      val jsonArray = jsonObject.getJSONArray("data")
      jsonArray.forEach(item => {
        val stockApiVO = JSONObject.parseObject(JSONObject.toJSONString(item), classOf[StockApiVo])
        STOCKS.add(stockApiVO)
      })

      println("stock总数:" + STOCKS.size)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println(e.getMessage)
    }
  }

  @throws[Exception]
  def loadToken(): Unit = {
    val tokenFile = new File(STOCK_TOKEN)
    if (tokenFile.exists && tokenFile.isFile && FileUtils.readFileToString(tokenFile, "UTF-8").trim == TOKEN) {
      loadAllStocks()
    }
    else {
      println(s"${STOCK_TOKEN}路径错误")
      System.exit(1)
    }
  }

}
