package zuk.tu_share.utils

import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.seg.common.Term
import org.apache.commons.io.FileUtils

import java.io.File
import scala.jdk.CollectionConverters.*

/***
 * 分词
 */
object FenCi_Util {

  var ignoreSet: Set[String] = null

  def createFenCi(textList: List[String]): Tuple2[List[(String, Int)], String] = {
    //"概念：DeepSeek概念，低空经济，商业航天，一带一路，央国企改革\n一级行业：建筑装饰\n二级行业：工程咨询服务Ⅱ\n三级行业：工程咨询服务Ⅲ"
    val ls = textList.flatMap(text=>{
      val termList = HanLP.segment(text).asScala.filter(doFilter(_)).groupBy(_.word).map(_._2.head).toList
      termList
    })

//    saveTerm(ls)
    
    val ls1 = ls.filter(doFilter(_)).map(_.word).groupBy(e=>e).toList.sortBy(_._2.size).reverse

    val ls2 = (if(ls1.size>100){
      ls1.take(200)
    }
    else {
      ls1
    })
    
    val str = ls2.map(tp=>{s"${tp._1}【${tp._2.size}】"}).mkString(",")


    (ls1.map(e=>(e._1, e._2.size)), s"总词数【${ls1.size}】>>>>" + str)
  }

  private def doFilter(term: Term): Boolean = synchronized {
    //忽略分词关键字
    if(ignoreSet == null){
      val ignoreKeywordFile = new File("ignoreKeyword.txt")
      ignoreSet = FileUtils.readLines(ignoreKeywordFile, "UTF-8").asScala.toSet
    }
    term.word.size > 1
      && !ignoreSet.contains(term.word)
  }
  
  private def saveTerm(terms: List[Term]): Unit = {
    val ignoreKeywordFile = new File("ignoreKeyword.txt")
    val keywords = FileUtils.readLines(ignoreKeywordFile, "UTF-8").asScala.toSet.toList
    FileUtils.writeLines(ignoreKeywordFile, (keywords ++ terms.map(_.word)).filter(_.trim.size>1).toSet.toList.asJava)
  }

}
