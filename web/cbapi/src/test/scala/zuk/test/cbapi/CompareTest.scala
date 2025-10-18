package zuk.test.cbapi

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.sast.cbapi.dto.ResultModel

import java.io.File

class CompareTest extends AnyFunSuite {

  test("compareTet") {
    println("a")
    val file1 = new File("")
    val file2 = new File("")

    if(file1.exists() && file2.exists() && file1.isFile && file2.isFile){

      val file1Content = FileUtils.readFileToString(file1, "UTF-8")
      val resultModel1 = JSONObject.parseObject(file1Content, classOf[ResultModel])

      val file2Content = FileUtils.readFileToString(file2, "UTF-8")
      val resultModel2 = JSONObject.parseObject(file2Content, classOf[ResultModel])

    }
    else {
      println("文件错误")
    }

  }

}
