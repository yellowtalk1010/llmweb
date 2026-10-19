package zuk.tu_share.utils

import org.apache.commons.io.FileUtils
import zuk.tu_share.ParseCammandParam

import java.io.File
import java.util.concurrent.ConcurrentHashMap

/***
 * 加载股票概念
 */
object Dataset_concept_dir {

  private val map = new ConcurrentHashMap[String, String]()

  /***
   * 根据tscode股票代码获取概念
   *
   * @param tsCode
   * @return
   */
  def getConcept(tsCode: String): String = {
    load()
    val v = map.get(tsCode)
    if(v==null){
      ""
    }
    else {
      v
    }
  }

  /***
   * 保存股票的板块和概念到文件中
   *
   * @param tsCode
   * @param concept
   */
  def storeConcept(tsCode: String, concept: String): Unit = {
    val f = new File(ParseCammandParam.param.engineInfo.concept_dir + File.separator + tsCode + "\\.txt")
    println(s"保存${tsCode}, ${Dataset_all_stocks_csv_file.getTsStock(tsCode).name}到${f.getAbsolutePath}, ${f.exists()}")
    FileUtils.writeStringToFile(f, concept, "UTF-8")
  }

  private def load(): Unit = synchronized {
    if(map.size() > 5000){
      return
    }
    val conceptDir = new File(ParseCammandParam.param.engineInfo.concept_dir)
    println("加载股票的板块和概念")
    conceptDir.listFiles().foreach(f=>{
      val tsCode = f.getName.replaceAll("\\.txt", "")
      val concept = FileUtils.readFileToString(f, "UTF-8")
      map.put(tsCode, concept)
      println(s"加载股票${tsCode}, ${Dataset_all_stocks_csv_file.getTsStock(tsCode).name},的板块和概念数据，路径：${f.getAbsolutePath}")
    })
  }



}
