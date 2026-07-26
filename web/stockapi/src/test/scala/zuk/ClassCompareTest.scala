package zuk

import org.apache.commons.codec.digest.DigestUtils
import org.scalatest.funsuite.AnyFunSuite

import java.io.{FileInputStream, IOException}
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
    jarClassPathFiles.asScala.foreach(p=>{
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
          println(s"文件不相同：cn\\${p.toString}")
        }
        else {
          println("文件相同")
        }
      }
    })

    println()
  }

}
