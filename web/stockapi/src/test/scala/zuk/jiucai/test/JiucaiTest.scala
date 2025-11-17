package zuk.jiucai.test

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.LoaderLocalStockData

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

class JiucaiTest extends AnyFunSuite {

  val codes = List(
    "001332"
  )
//    ++ HLTrade.tradeStockCodes.take(20)

  test("config"){
    LoaderLocalStockData.loadToken()
    val stocks = LoaderLocalStockData.STOCKS
    stocks.asScala.map(_.getJys).toSet.foreach(println)
    codes.filter(e=>{
      !stocks.asScala.map(_.getApi_code).contains(e)
    }).foreach(e=>{
      println(s"未找到：${e}")
    })
    val filterStocks = stocks.asScala.filter(e=>{
      codes.contains(e.getApi_code)
    })
    val list = filterStocks.map(e=>{
      var code = ""
      if(e.getJys.equals("SZ")){
        code = s"0.${e.getApi_code}"
      }
      else if(e.getJys.equals("SH")) {
        code = s"1.${e.getApi_code}"
      }
      (code, e.getName)
    }).filter((c,n)=>StringUtils.isNotBlank(c))

    val c1 = zuk.jiucai.test.Config.createConfigJsonFile(list.toList)
    val json = JSONObject.toJSONString(c1, Feature.PrettyFormat)
    println(json)
    val file = new File("stockapi/src/test/resources/jiucai/config.json")
    FileUtils.write(file, json, "UTF-8")
  }

}
