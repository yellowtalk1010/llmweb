package zuk.tu_share.send

import zuk.tu_share.module.IModel
import zuk.tu_share.utils.LicenseUtil

/***
 * 控制台输出
 */
class Console extends ISend {

  override def doSend(list: List[IModel]): Unit = {
    try {
      list.groupBy(_.getClass.getSimpleName).filter(_._2.size > 0).toList.sortBy(_._2.head.winRate).reverse.foreach(tp2 => {
        val moduleName = tp2._1
        val moduleList = tp2._2

        val stockDtos = moduleList.map(_.getStockDto())

        if (LicenseUtil.check()) {
          //如果许可通过，则打印出结果
          println(moduleName)
          println(stockDtos.map(_.tsStock).map(e => s"${e.ts_code}, ${e.name}").mkString("\n"))
        }
      })
    }
    catch
      case exception: Exception =>
  }

}
