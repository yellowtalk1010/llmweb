package zuk.consolidation

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import org.apache.commons.io.FileUtils
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.module.MA10_Model
import zuk.tu_share.utils.Dataset_module_history_rtk_dir

import java.util

import java.io.File
import scala.jdk.CollectionConverters.*

object MainForceAccumulationScanner {

  val ma10File = new File(ParseCammandParam.param.engineInfo.MODEL_BACK_TEST_RESULT_dir + File.separator + "MA10_MODEL")
  
  /***
   * 预计主力吸筹
   * @param args
   */
  def main(args: Array[String]): Unit = {
    
    if(ma10File.exists()){
      return 
    }
    
    val resList = Dataset_module_history_rtk_dir.loadModelAnalysisDataSet.values.filter(_.size > 60).map(ls=>{
      val model = new MA10_Model
      model.run(ls)
      if(model.stockDto!=null){
        Some(model.stockDto)
      }
      else {
        Option.empty
      }
    }).filter(! _.isEmpty)
      .map(_.get)
      .toList
      .sortBy(e=>(e.tradedate, e.tsStock.ts_code))
    
    val lines = resList.map(e=>{
      val map = new util.HashMap[String, String]()
      map.put("stockCode", e.tsStock.ts_code)
      map.put("stockName", e.tsStock.name)
      map.put("stockType", "MA10_MODEL")
      map.put("tradedate", e.tradedate)
      map.put("status", "")
      val l = JSONObject.toJSONString(map, Feature.LargeObject)
      l
    })
    
    
    println(s"预测的主力吸筹结果写入：${ma10File.getAbsolutePath}")
    FileUtils.writeLines(ma10File, lines.asJava, "UTF-8")
    
  }
  
}
