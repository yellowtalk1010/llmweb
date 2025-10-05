package zuk.stockapi

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.io.FileUtils

import java.io.File
import java.text.SimpleDateFormat
import java.util
import java.util.{ArrayList, Date, List}
import scala.beans.BeanProperty

import scala.jdk.CollectionConverters.*

object LoaderLocalStockData {

  val TOKEN = "36c92182f783f08005017f78e7a264608a82952f8b91de2a"

  val STOCK_DATA_DIR_PATH = "d:\\development\\github\\llmweb1\\web\\stocks"
//  val STOCK_DATA_DIR_PATH = "D:\\AAAAAAAAAAAAAAAAAAAA\\github\\llmweb1\\web\\stocks"
  val STOCK_TOKEN = STOCK_DATA_DIR_PATH + File.separator + "token"
  val STOCK_ALL = STOCK_DATA_DIR_PATH + File.separator + "all_stock.json"
  val STOCK_DAY = STOCK_DATA_DIR_PATH + File.separator + "days"
  val STOCK_MINUTE = STOCK_DATA_DIR_PATH + File.separator + "minute"
  //val STOCK_MA = STOCK_DATA_DIR_PATH + File.separator + "ma"

  val STOCKS: util.List[StockApiVo] = new util.ArrayList[StockApiVo]

  loadToken()
  loadAllStocks()
  printInfo()

  private def loadAllStocks(): Unit = synchronized {
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
        if(!STOCKS.asScala.map(_.getApi_code).contains(stockApiVO.getApi_code)){
          STOCKS.add(stockApiVO)
        }

      })

      println("stock总数:" + STOCKS.size)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println(e.getMessage)
    }
  }

  @throws[Exception]
  def loadToken(): Unit = synchronized {
    val tokenFile = new File(STOCK_TOKEN)
    if (tokenFile.exists && tokenFile.isFile && FileUtils.readFileToString(tokenFile, "UTF-8").trim == TOKEN) {
      if (STOCKS.size()==0){
        loadAllStocks()
      }
    }
    else {
      println(s"${STOCK_TOKEN}路径错误")
      System.exit(1)
    }
  }

  def printInfo(): Unit = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    val dt = sdf.format(new Date)
    var url = "https://stockapi.com.cn/v1/base/bkjj?endDate=" + dt + "&startDate=" + dt + "&type=1&token=" + LoaderLocalStockData.TOKEN
    System.out.println("热门板块：" + url)
    url = "https://stockapi.com.cn/v1/base/bkCodeList?endDate=" + dt + "&startDate=" + dt + "&bkCode=880431&token=" + LoaderLocalStockData.TOKEN
    System.out.println("热门板块个股：" + url)
    url = "https://stockapi.com.cn/v1/base/all?token=" + LoaderLocalStockData.TOKEN
    System.out.println("A股列表数据查询: " + url)
  }

}
