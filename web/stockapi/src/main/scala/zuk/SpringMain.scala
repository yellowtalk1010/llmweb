package zuk

import org.apache.ibatis.annotations.Mapper
import org.mybatis.spring.mapper.MapperScannerConfigurer
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.{SpringApplication, WebApplicationType}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.{AnnotationConfigApplicationContext, ClassPathScanningCandidateComponentProvider}
import org.springframework.core.`type`.filter.AnnotationTypeFilter
import org.springframework.core.io.support.ResourcePropertySource
import org.springframework.stereotype.Component
import zuk.sast.controller.component.{ApplicationProperties, SpringContextUtil}
import zuk.tu_share.DataFrame

import scala.jdk.CollectionConverters.*

@SpringBootApplication
class SpringMain {

}

object SpringMain {

  /***
   * 直接运行springboot，但是不启动tomcat
   * @param args
   */
  def main(args: Array[String]): Unit = {

    new SpringApplicationBuilder()
      .sources(classOf[SpringMain])
      .web(WebApplicationType.NONE)
      .properties(
        "spring.config.location=file:D:/development/github/llmweb/web/boot/src/main/resources/application.properties"
      )
      .run(args: _*)

  }

  /***
   * 第一种方式
   * @param args
   */
  def test_main(args: Array[String]): Unit = {

    val annotationConfigApplicationContext = new AnnotationConfigApplicationContext

    val scanner = new ClassPathScanningCandidateComponentProvider(false)
    scanner.addIncludeFilter(new AnnotationTypeFilter(classOf[Component]))

    val candidates = scanner.findCandidateComponents("zuk.**")
    candidates.asScala.foreach(bd=>{
      val beanClassName =  bd.getBeanClassName
      println(beanClassName)
      val clazz = Class.forName(beanClassName)
      annotationConfigApplicationContext.registerBeanDefinition(clazz.getSimpleName, bd)
    })

    //注册mapper
    val mapperScanner = new MapperScannerConfigurer()
    mapperScanner.setApplicationContext(annotationConfigApplicationContext)
    mapperScanner.setBasePackage("zuk.sast.controller.mapper")
    annotationConfigApplicationContext.getBeanFactory.registerSingleton("mapperScannerConfigurer", mapperScanner)

    //注册资源文件
    val source = new ResourcePropertySource("file:/d:/development/github/llmweb/web/boot/src/main/resources/application.properties")
    val env = annotationConfigApplicationContext.getEnvironment
    env.getPropertySources.addLast(source)

    annotationConfigApplicationContext.refresh()
    SpringContextUtil.context = annotationConfigApplicationContext

    val applicationProperties = SpringContextUtil.context.getBean(classOf[ApplicationProperties])
//    applicationProperties.init()
  }

}
