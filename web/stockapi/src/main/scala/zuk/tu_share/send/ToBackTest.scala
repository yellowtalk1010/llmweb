package zuk.tu_share.send

import zuk.tu_share.module.IModel

import scala.collection.mutable.ListBuffer

object ToBackTest {
  val backTestList = ListBuffer[IModel]()
}

/***
 * 收集回测数据
 */
class ToBackTest extends ISend {

  override def doSend(list: List[IModel]): Unit = {

    list.groupBy(_.getClass.getSimpleName).filter(_._2.size > 0).toList.sortBy(_._2.head.winRate).reverse.foreach(tp2 => {
      val moduleName = tp2._1
      val moduleList = tp2._2
      
      ToBackTest.backTestList ++= moduleList //在内存中收集引擎的分析结果，用于回测
    })

  }

}
