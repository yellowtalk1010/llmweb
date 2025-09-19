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

  val STOCKS: util.List[StockApiVO] = new util.ArrayList[StockApiVO]


  private def loadAllStocks(): Unit = {
    try {
      val file = new File(STOCK_ALL)
      val content = FileUtils.readFileToString(file, "UTF-8")
      val jsonObject = JSONObject.parseObject(content)
      val jsonArray = jsonObject.getJSONArray("data")
      jsonArray.forEach(item => {
        val stockApiVO = JSONObject.parseObject(JSONObject.toJSONString(item), classOf[StockApiVO])
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
  def load(): Unit = {
    val tokenFile = new File(STOCK_TOKEN)
    if (tokenFile.exists && tokenFile.isFile && FileUtils.readFileToString(tokenFile, "UTF-8").trim == TOKEN) loadAllStocks()
    else {
      println("STOCK_PATH路径错误")
      System.exit(0)
    }
  }

}

class StockApiVO(@BeanProperty api_code: String,
                 @BeanProperty jys: String,
                 @BeanProperty name: String,
                 @BeanProperty gl: String)
