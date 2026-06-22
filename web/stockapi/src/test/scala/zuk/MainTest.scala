package zuk

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite

import java.io.File

class MainTest extends AnyFunSuite {

  test("main推荐测试"){
    val args = Array("-path", "D:\\development\\github\\stockapi\\", "-json")
    zuk.Main.main(args)
  }

  test("main回测测试") {
    val args = Array("-path", "D:\\development\\github\\stockapi\\", "-json", "-back")
    zuk.Main.main(args)
  }

  test("pom依赖生成器"){
    val file = new File("C:\\Users\\5132\\Desktop\\JavaClaw\\app-1.0.0-SNAPSHOT\\BOOT-INF\\lib")
    val list = file.listFiles().toList.sortBy(_.getName)

    val groupId = "a"
    val artifactId = "b"
    val version = "1.0.0"

    list.zipWithIndex.foreach(e=>{
      val filename = e._1.getName
      val index = e._2
      val path = e._1.getAbsolutePath.replaceAll("\\\\", "/")

      val myArtifactId = s"${artifactId}-${index}"

      /***
       *         <dependency>
       *             <groupId>org.junit.platform</groupId>
       *             <artifactId>junit-platform-launcher</artifactId>
       *             <version>6.0.3</version>
       *             <scope>test</scope>
       *        </dependency>
       */

      val str = {
        s"<!-- ${filename} -->\n" +
        "<dependency>\n" +
        s"<groupId>${groupId}</groupId>\n" +
        s"<artifactId>artifactId${myArtifactId}</artifactId>\n" +
        s"<version>${version}</version>\n" +
        "<systemPath>${project.basedir}/libs/" + s"${filename}</systemPath>\n" +
        "<scope>system</scope>\n" +
        "</dependency>\n"
      }
      println(str)
    })

    println("\n\n\n-------------------------------------m2依赖----------------------------------------\n\n\n")

    list.zipWithIndex.foreach(e=>{
      val filename = e._1.getName
      val index = e._2

      val myArtifactId = s"${artifactId}-${index}"

      val str = {
        s"<!-- ${filename} -->\n" +
          "<dependency>\n" +
          s"<groupId>${groupId}</groupId>\n" +
          s"<artifactId>${myArtifactId}</artifactId>\n" +
          s"<version>${version}</version>\n" +
          "</dependency>\n"
      }

      val jarPath = s"${groupId}/${myArtifactId}/${version}/${myArtifactId}-${version}.jar"
      val pomPath = s"${groupId}/${myArtifactId}/${version}/${myArtifactId}-${version}.pom"

      val jarFile = new File(jarPath)
      val pomFile = new File(pomPath)
      if(jarFile.exists()){
        jarFile.delete()
      }
      if(pomFile.exists()){
        pomFile.delete()
      }
      FileUtils.copyFile(e._1, jarFile)
      pomFile.createNewFile()

      println(str)

    })

  }
}
