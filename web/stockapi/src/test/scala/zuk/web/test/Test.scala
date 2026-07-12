package zuk.web.test

import com.hankcs.hanlp.HanLP
import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import scala.jdk.CollectionConverters.*

class Test extends AnyFunSuite {


  test("分词测试") {

    val termList = HanLP.segment("概念：DeepSeek概念，低空经济，商业航天，一带一路，央国企改革\n一级行业：建筑装饰\n二级行业：工程咨询服务Ⅱ\n三级行业：工程咨询服务Ⅲ")
    termList.asScala.foreach(t=>{
      println(t.word)
    })

  }

}

