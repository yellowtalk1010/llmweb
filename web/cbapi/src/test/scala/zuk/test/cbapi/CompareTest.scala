package zuk.test.cbapi

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter.Feature
import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite
import zuk.sast.cbapi.dto.{DefectResultModel, FunctionModel, ResultModel}

import scala.jdk.CollectionConverters.*
import java.io.File

class CompareTest extends AnyFunSuite {

  test("数据比较") {

    val file1 = new File("D:\\development\\github\\webb\\web\\json_result\\8114-cb-20251018-104434.json")
    val file2 = new File("D:\\development\\github\\webb\\web\\json_result\\8114-zuk-20251018-104100.json")

    if(file1.exists() && file2.exists() && file1.isFile && file2.isFile){

      val file1Content = FileUtils.readFileToString(file1, "UTF-8")
      val resultModel1 = JSONObject.parseObject(file1Content, classOf[ResultModel])

      val file2Content = FileUtils.readFileToString(file2, "UTF-8")
      val resultModel2 = JSONObject.parseObject(file2Content, classOf[ResultModel])

      println(s"${file1.getName} 比较 ${file2.getName}")
      compare(resultModel1, resultModel2)

      println(s"${file2.getName} 比较 ${file1.getName}")
      compare(resultModel2, resultModel1)

    }
    else {
      println("文件错误")
    }

  }

  private def compare(resultModel1: ResultModel, resultModel2: ResultModel): Unit = {
    compareDefectResultModel(resultModel1.getDefectResultModelList.asScala.toList, resultModel2.getDefectResultModelList.asScala.toList)
    compareFunctionMode(resultModel1.getFunctionModelList.asScala.toList, resultModel2.getFunctionModelList.asScala.toList)
  }

  private def compareDefectResultModel(list1: List[DefectResultModel], list2: List[DefectResultModel]): Unit = {
    val desc = "compareDefectResultModel"
    list1.foreach(drm1=>{
      val str1 = JSONObject.toJSONString(drm1, Feature.PrettyFormat)
      val ls = list2.filter(drm2=>{
        drm1.getLine.equals(drm2.getLine)
        && drm1.getDescript.equals(drm2.getDescript)
        && drm1.getCategoryId.equals(drm2.getCategoryId)
        && drm1.getCategoryName.equals(drm2.getCategoryName)
        && drm1.getRuleName.equals(drm2.getRuleName)
        && drm1.getRuleId.equals(drm2.getRuleId)
        && drm1.getRuleCatagory.equals(drm2.getRuleCatagory)
        && drm1.getRuleTags.asScala.toList.sorted.mkString(";").equals(drm2.getRuleTags.asScala.toList.sorted.mkString(";"))
        && drm1.getLevel.equals(drm2.getLevel)
        && drm1.getFilepath.equals(drm2.getFilepath)
        && drm1.getLanguage.equals(drm2.getLanguage)
        && drm1.getMd5.equals(drm2.getMd5)
        && drm1.getChildren.size()==drm2.getChildren.size()
        && drm1.getChildren.asScala.flatMap(e=>List(e.getFilePath,e.getLine+"",e.getDescript)).mkString(";")
          .equals(drm2.getChildren.asScala.flatMap(e=>List(e.getFilePath, e.getLine+"", e.getDescript)).mkString(";"))
      })
      if(ls.size==0){
        println(s"${desc}：未找到匹配，${str1}")
      }
      else if(ls.size==1){
        //OK
      }
      else {
        println(s"${desc}：包含多个匹配，${str1}")
      }
    })
  }

  private def compareFunctionMode(list1: List[FunctionModel], list2: List[FunctionModel]): Unit = {
    val desc = "compareFunctionMode"
    list1.foreach(fm1=>{
      val str1 = JSONObject.toJSONString(fm1, Feature.PrettyFormat)
      val ls = list2.filter(fm2=>{
        fm1.getFilePath.equals(fm2.getFilePath)
        && fm1.getLine.asScala.toList.sorted.mkString(";").equals(fm2.getLine.asScala.toList.sorted.mkString(";"))
        && fm1.getFunctionName.equals(fm2.getFunctionName)
      })
      if(ls.size==0){
        println(s"${desc}：未找到匹配，${str1}")
      }
      else if(ls.size==1){
        //OK
      }
      else {
        println(s"${desc}：包含多个匹配，${str1}")
      }
    })
  }

}
