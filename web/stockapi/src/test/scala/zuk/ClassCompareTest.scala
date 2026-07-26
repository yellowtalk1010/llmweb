package zuk

import org.scalatest.funsuite.AnyFunSuite

import java.io.IOException
import java.nio.file.{Files, Path, Paths}
import java.util
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
    val jarClassPathMap = new util.HashMap[String, Path]()
    jarClassPathFiles.asScala.foreach(p=>{
      jarClassPathMap.put(p.toString, p)
    })

    val targetClassPath = "D:\\development\\github\\webb\\web\\cobot-parsers\\target\\classes\\cn"
    val targetClassPathFiles = getAllFiles(targetClassPath)
    val targetClassPathMap = new util.HashMap[String, Path]()
    targetClassPathFiles.asScala.foreach(p=>{
      targetClassPathMap.put(p.toString, p)
    })

    
    println()
  }

}
