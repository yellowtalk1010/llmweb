package zuk.tu_share.utils

import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.seg.common.Term

import scala.jdk.CollectionConverters.*
/***
 * 分词
 */
object HanLPUtil {

  def createFenCi(textList: List[String]): String = {
    //"概念：DeepSeek概念，低空经济，商业航天，一带一路，央国企改革\n一级行业：建筑装饰\n二级行业：工程咨询服务Ⅱ\n三级行业：工程咨询服务Ⅲ"
    val ls = textList.flatMap(text=>{
      val termList = HanLP.segment(text).asScala.toSet.toList
      termList
    })

    val ls1 = ls.filter(doFilter(_)).map(_.word).groupBy(e=>e).toList.sortBy(_._2.size).reverse

    val str = (if(ls1.size>100){
      ls1.take(200)
    }
    else {
      ls1
    }).map(tp=>{
      s"${tp._1}【${tp._2.size}】"
    }).mkString(",")

    s"总词数【${ls1.size}】>>>>" + str
  }

  private def doFilter(term: Term): Boolean = {
    //忽略分词关键字
    val ignoreSet = Set[String]("概念", "一级", "二级", "三级", "行业", "名称", "任务", "板块", "股票", "所属", "其他", "设备", "建设", "500", "重仓", "国企", "改革", "通用")
    term.word.size > 1 && !ignoreSet.contains(term.word)
  }

}
