package zuk

import org.apache.commons.codec.digest.DigestUtils
import org.scalatest.funsuite.AnyFunSuite

import java.io.{File, FileInputStream, IOException}
import java.nio.file.{Files, Path, Paths}
import java.util
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*

class ClassCompareTest extends AnyFunSuite {

  @throws[IOException]
  def getAllFiles(path: String): java.util.List[Path] = {
    val rootPath = Paths.get(path)
    val paths = Files.walk(rootPath)
    val ls = paths.toList.asScala.filter(p=>p.toFile.isFile).map(p=>{
      val relativizePath = rootPath.relativize(p)
      val str = relativizePath.toString
      relativizePath
    }).toList.sortBy(e=>(e.toString)).asJava
    ls
  }

  test("class比较") {
    val jarClassPath = "C:\\Users\\5132\\.m2\\repository\\zuk\\cbsast\\merge-93\\2.0.0\\merge-93-2.0.0\\cn"
    val jarClassPathFiles = getAllFiles(jarClassPath)
    println(s"${jarClassPathFiles.size()}")
    val jarClassPathMap = new util.HashMap[String, Path]()
    jarClassPathFiles.asScala.foreach(p=>{
      jarClassPathMap.put(p.toString, p)
    })

    val targetClassPath = "D:\\development\\github\\webb\\web\\cobot-parsers\\target\\classes\\cn"
    val targetClassPathFiles = getAllFiles(targetClassPath)
    println(s"${targetClassPathFiles.size()}")
    val targetClassPathMap = new util.HashMap[String, Path]()
    targetClassPathFiles.asScala.foreach(p=>{
      targetClassPathMap.put(p.toString, p)
    })

    val num = new AtomicInteger(0)
    val num1 = new AtomicInteger(0)
    val num2 = new AtomicInteger(0)
    jarClassPathFiles.asScala.filter(p=> !p.toString.contains("utils\\linetracker")  ).filter(p=>{
      this.ignoreFiles().filter(f=>f.endsWith(p.toString)).size == 0
    }).foreach(p=>{
      val path = targetClassPathMap.get(p.toString)
      if(path==null){
        println(s"${num.addAndGet(1)}缺少文件：cn\\${p.toString}")
      }
      else {
        val p1 = jarClassPath + "\\" + p.toString
        val fis1 = new FileInputStream(p1)
        val md5_1 = DigestUtils.md5Hex(fis1)

        val p2 = targetClassPath  + "\\" + path.toString
        val fis2 = new FileInputStream(p2)
        val md5_2 = DigestUtils.md5Hex(fis2)


        if(!md5_1.equals(md5_2)){
          println(s"${num1.addAndGet(1)}文件不相同：${p2}")
          val javaPath = if(p2.contains("$")){
            s"D:\\development\\github\\webb\\web\\cobot-parsers\\src\\main\\java\\cn\\${path.toString.split("\\$")(0)}.java"
          }
          else {
            s"D:\\development\\github\\webb\\web\\cobot-parsers\\src\\main\\java\\cn\\${path.toString.replaceAll(".class",".java")}"
          }
//          val javaPath = s"D:\\development\\github\\webb\\web\\cobot-parsers\\src\\main\\java\\cn\\${path.toString.replaceAll(".class",".java")}"
          if(new File(javaPath).exists()){
            println(s"           ${javaPath}，${new File(javaPath).exists()}")
//            new File(javaPath).delete()
          }
        }
        else {
          //println(s"${num2.addAndGet(1)}文件相同")
        }
      }
    })

    println()
  }

  private def ignoreFiles(): Set[String] = {
    Set[String](
      "cn\\net\\cobot\\parsers\\cparser\\common\\ParserUtility.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\MakeFileParser.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CXXCheckParamConverter.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CheckConfig.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CheckParam$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CheckParam$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CheckParam.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CobotParserConfig.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$BitFieldInterpretation.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$CharInterpretation.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$IntrinsicType_PtrdiffT.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$IntrinsicType_SizeT.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$IntrinsicType_WcharT.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$RightShiftBehaviour.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$TypeSizeAndAlignment.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\LostFileHeader.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$3.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$4.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$5.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\TypeCompare.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\AbstractCExtension.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CCProjectNature.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CConventions.class"
    )
  }

}
