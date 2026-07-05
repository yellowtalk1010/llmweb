package zuk

import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

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

    val file = new File("C:\\cobot-install-创景\\cobot-v4.3\\resources\\restapp\\cobot-web-4.0.0-SNAPSHOT\\BOOT-INF\\lib")
    val projectName = "web"
    val version = "1.0.0"

    val list = file.listFiles().toList.sortBy(_.getName)

    val repositoryPath = ".m2/repository/"

    val groupId = "zuk"
    val artifactId = "merge"

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
        s"<groupId>${groupId}.${projectName}</groupId>\n" +
        s"<artifactId>artifactId${myArtifactId}</artifactId>\n" +
        s"<version>${version}</version>\n" +
        "<systemPath>${project.basedir}/libs/" + s"${filename}</systemPath>\n" +
        "<scope>system</scope>\n" +
        "</dependency>\n"
      }
      println(str)
    })

    println("\n\n\n-------------------------------------m2依赖----------------------------------------\n\n\n")

    val strList = new ListBuffer[String]
    list.zipWithIndex.foreach(e=>{
      val filename = e._1.getName
      val index = e._2

      val myArtifactId = s"${artifactId}-${index}"

      val str = {
        s"""
           |<!-- ${filename} -->
           |<dependency>
           |    <groupId>${groupId}.${projectName}</groupId>
           |    <artifactId>${myArtifactId}</artifactId>
           |    <version>${version}</version>
           |</dependency>
           |""".stripMargin
      }

      val jarPath = s"${groupId}/${projectName}/${myArtifactId}/${version}/${myArtifactId}-${version}.jar"
      val pomPath = s"${groupId}/${projectName}/${myArtifactId}/${version}/${myArtifactId}-${version}.pom"

      val jarFile = new File(repositoryPath + jarPath)
      val pomFile = new File(repositoryPath + pomPath)
      if(jarFile.exists()){
        jarFile.delete()
      }
      if(pomFile.exists()){
        pomFile.delete()
      }
      FileUtils.copyFile(e._1, jarFile)

      val pomFileText =
        s"""<?xml version="1.0" encoding="UTF-8"?>
          |<project xmlns="http://maven.apache.org/POM/4.0.0"
          |         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          |         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
          |    <modelVersion>4.0.0</modelVersion>
          |
          |    <groupId>${groupId}.${projectName}</groupId>
          |    <artifactId>${myArtifactId}</artifactId>
          |    <version>${version}</version>
          |
          |</project>
          |""".stripMargin
      //pomFile.createNewFile()
      FileUtils.writeLines(pomFile, pomFileText.lines().toList)

      println(str)

      strList += str

    })

    val parentGroupId = "zuk"
    val parentArtifactId = s"${artifactId}-parent"
    val parentStartStr =
      s"""<project xmlns="http://maven.apache.org/POM/4.0.0"
        |        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        |<modelVersion>4.0.0</modelVersion>
        |
        |<groupId>${parentGroupId}.${projectName}</groupId>
        |<artifactId>${parentArtifactId}</artifactId>
        |<version>${version}</version>
        |
        |<packaging>pom</packaging>
        |
        |<dependencies>
        |
        |""".stripMargin

    //在列表的第一列加入
    strList.prepend(parentStartStr)

    //在列表的最后列加入
    val parentEndStr =
      """
        |
        |</dependencies>
        |</project>
        |""".stripMargin

    strList.append(parentEndStr)


    FileUtils.writeLines(new File(repositoryPath + s"${parentGroupId}/${projectName}/${parentArtifactId}/${version}/${parentArtifactId}-${version}.pom"), strList.toList.asJava)

    println(s"保存路径：${new File(repositoryPath).getAbsolutePath}")
  }
}
