package zuk.tu_share.utils

import scala.jdk.CollectionConverters.*

object ModuleUtil {

  def handule(): Unit = {
    HmDetailUtil.loadData()
    TopInstUtil.loadData()
    HmDetailUtil.hmDetailMap.map(_._1).toList.sorted.foreach(tradedate=>{

    })
    val hmDetailList = HmDetailUtil.hmDetailMap.flatMap(_._2).toList.sortBy(_.trade_date)
    hmDetailList
  }

}
