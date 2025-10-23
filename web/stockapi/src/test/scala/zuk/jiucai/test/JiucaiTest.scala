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
    "300081",
    "300158",
    "301501",
    "688195",
    "000838",
    "600793",
    "603280",
    "002305",
    "300689",
    "002342",
    "601606",
    "603059",
    "002857",
    "600527",
    "301052",
    "603398",
    "300797",
    "605167",
    "000612",
    "001896",
    "688400",
    "688710",
    "002463",
    "300620",
    "301308",
    "605178",
    "002050",
    "300729",
    "002708",
    "688652",
    "000065",
    "000789",
    "300287",
    "300946",
    "600192",
    "600589",
    "600641",
    "601390",
    "603601",
    "920046",
    "002456",
    "605218",
    "002094",
    "302132",
    "001380",
    "600410",
    "002104",
    "300718",
    "002945",
    "000831",
    "603106",
    "920571",
    "300047",
    "300065",
    "300019",
    "601138",
    "603019",
    "601899",
    "603011",
    "002896",
    "000567",
    "002423",
    "300570",
    "000617",
    "300499",
    "300903",
    "002177",
    "003040",
    "300348",
    "002235",
    "300502",
    "300059",
    "600879",
    "688041",
    "301293",
    "301035",
    "600733",
    "600765",
    "002297",
    "688176",
    "002072",
    "300738",
    "600418",
    "600571",
    "002582",
    "603169",
    "002194",
    "300674",
    "600764",
    "300153",
    "601398",
    "002119",
    "603887",
    "001158",
    "002213",
    "002402",
    "002065",
    "002085",
    "002855",
    "300045",
    "300339",
    "600020",
    "600159",
    "603629",
    "003005",
    "002354"

  )

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
