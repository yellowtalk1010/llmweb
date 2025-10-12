package zuk.web.test

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class Test extends AnyFunSuite {

  test("web") {
    val libDirFile = new File("D:\\development\\github\\webb\\web\\zuk\\lib")
    if(!libDirFile.exists()){
      throw new Exception("路径不存在")
    }
    val files = libDirFile.listFiles().sortBy(_.getName)
    files.map(_.getName).foreach(println)
    println(files.size)
    //repository
    val num = AtomicInteger(0)
    files.map(srcFile=>{
      num.incrementAndGet()
      val groupId = "com.google.zuk"
      val artifactId = s"web_${num.get()}"
      val version = "1.0.0"
      val filename = s"${artifactId}-${version}"

      val targetPath = s"repository/${groupId.replaceAll("\\.","/")}/${artifactId}/${version}"
      val pomPath = targetPath + "/" + filename + ".pom"
      val jarPath = targetPath + "/" + filename + ".jar"

      new File(pomPath).getParentFile.mkdirs()
      new File(pomPath).createNewFile()
      println(pomPath + "，完成")

      FileUtils.copyFile(srcFile, new File(jarPath))
      println(jarPath + "，完成")

      println()

      val depStr = s"<!-- ${srcFile.getName} -->\n" +
                   "<dependency>\n" +
                   s"    <groupId>${groupId}</groupId>\n" +
                   s"    <artifactId>${artifactId}</artifactId>\n" +
                   s"    <version>${version}</version>\n" +
                   "</dependency>\n"

      depStr
    }).foreach(println)

  }

}

