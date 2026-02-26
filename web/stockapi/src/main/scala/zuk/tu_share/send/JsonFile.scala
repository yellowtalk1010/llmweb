package zuk.tu_share.send

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import org.apache.commons.io.FileUtils
import zuk.tu_share.ParseCammandParam
import zuk.tu_share.module.IModel
import zuk.tu_share.utils.LicenseUtil

import java.io.File
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import scala.jdk.CollectionConverters.*

/***
 * 保存json文件
 */
class JsonFile extends ISend {

  override def doSend(list: List[IModel]): Unit = {
    try {

      if(ParseCammandParam.param.json
        && LicenseUtil.check()){
        //校验命令、 许可校验

        val arr = list.map(m => {

          val map = new util.HashMap[String, String]()

          val dto = m.getStockDto()

          val limitUp = dto.limitUp
          val limitDown = dto.limitDown
          val turnoverRate = dto.turnoverRate //活跃

          map.put("limitUp", limitUp)
          map.put("limitDown", limitUp)
          map.put("turnoverRate", s"${turnoverRate}活跃")

          val ts_code = dto.tsStock.ts_code //代码
          val name = dto.tsStock.name //名称
          val area = dto.tsStock.area //区域
          val industry = dto.tsStock.industry //行业

          map.put("ts_code", ts_code)
          map.put("name", name)
          map.put("area", area)
          map.put("industry", industry)

          val modWinRate = m.winRate //模型胜率
          val modDesc = m.desc() //模型描述
          val modClsName = m.getClass.getSimpleName.toUpperCase //模型代码

          map.put("modWinRate", s"${modWinRate}")
          map.put("modDesc", modDesc)
          map.put("modClsName", modClsName)

          map
        }).asJava

        val json = JSONObject.toJSONString(arr, Feature.PrettyFormat)
        val sdf = new SimpleDateFormat("yyyyMMdd_HH_mm_ss")
        val filepath = s"result_json/${sdf.format(new Date)}.json"
        FileUtils.write(new File(filepath), json, "UTF-8")

      }

    }
    catch
      case exception: Exception =>
  }

}
