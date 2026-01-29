package zuk.tu_share.send

import zuk.tu_share.backtest.BackTest
import zuk.tu_share.module.IModel

/***
 * 收集回测数据
 */
class ToBackTest extends ISend {

  override def doSend(list: List[IModel]): Unit = {

    list.groupBy(_.getClass.getSimpleName).filter(_._2.size > 0).toList.sortBy(_._2.head.winRate).reverse.foreach(tp2 => {
      val moduleName = tp2._1
      val moduleList = tp2._2

      BackTest.backTestList ++= moduleList  //收集回测数据

    })

  }

}
