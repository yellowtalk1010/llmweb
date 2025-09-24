package zuk.stockapi

import com.alibaba.fastjson2.JSONObject
import zuk.stockapi.utils.HttpClientUtil

import java.util.concurrent.atomic.AtomicInteger

class Download {

  def download(url: String, maxTime: Int = 5): String = {
    var response: String = null
    var isBreak = false
    val looptime = new AtomicInteger(0) //循环次数
    while (!isBreak && looptime.get() < maxTime) {
      try {
        if (looptime.get() > 0) {
          println(s"第${looptime.get()}次重试请求:${url}")
        }
        response = HttpClientUtil.sendGetRequest(url)
        val jsonObject = JSONObject.parseObject(response)
        val resCode = jsonObject.get("code").asInstanceOf[Integer]
        val resMsg = jsonObject.get("msg").asInstanceOf[String]
        if (resCode.toString.equals("20000") && resMsg == "success") {
          isBreak = true
          return response
        }
        else {
          println(s"数据采集异常（${looptime.incrementAndGet()}次），${response}，${url}")
          Thread.sleep(10) //等待500毫秒继续请求
        }
      }
      catch
        case exception: Exception =>
          exception.printStackTrace()
          looptime.incrementAndGet()
    }
    null
  }
}
