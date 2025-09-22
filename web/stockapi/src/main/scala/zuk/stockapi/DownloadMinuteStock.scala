package zuk.stockapi

import com.alibaba.fastjson2.{JSONArray, JSONObject}
import org.apache.commons.lang3.StringUtils

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/***
 * 获取交易日分钟信息，在2点30分时执行
 */
object DownloadMinuteStock extends Download {

  def run(stockList: List[StockApiVo]) = {
    stockList.foreach(stockApiVO=>{
      try {
        val path = LoaderLocalStockData.STOCK_MINUTE + File.separator + stockApiVO.getApi_code + ".jsonl"
        val url = s"https://www.stockapi.com.cn/v1/base/min?token=${LoaderLocalStockData.TOKEN}&code=${stockApiVO.getApi_code}&all=1"
        val response = super.download(url,2)
        if (StringUtils.isNotEmpty(response)) {
          val jsonArray = JSONObject.parseObject(response).get("data").asInstanceOf[JSONArray]

        }
      }
      catch
        case exception: Exception =>
    })
  }

}
