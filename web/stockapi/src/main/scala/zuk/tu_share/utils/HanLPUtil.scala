package zuk.tu_share.utils

import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.seg.common.Term

import scala.jdk.CollectionConverters.*
/***
 * 分词
 */
object HanLPUtil {

  def createFenCi(text: String): List[Term] = {
    //"概念：DeepSeek概念，低空经济，商业航天，一带一路，央国企改革\n一级行业：建筑装饰\n二级行业：工程咨询服务Ⅱ\n三级行业：工程咨询服务Ⅲ"
    val termList = HanLP.segment(text)
    termList.asScala.filter(doFilter(_)).toList
  }

  private def doFilter(term: Term): Boolean = {
    val set = Set[String]("概念", "一级", "二级", "三级")
    term.word.size > 1 && !set.contains(term.word)
  }

}
