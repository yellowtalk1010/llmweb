package zuk

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.{AnnotationConfigApplicationContext, ClassPathScanningCandidateComponentProvider}
import org.springframework.core.`type`.filter.AnnotationTypeFilter
import org.springframework.core.io.support.ResourcePropertySource
import org.springframework.stereotype.Component
import zuk.sast.controller.component.ApplicationProperties
import zuk.tu_share.DataFrame
import zuk.utils.SpringContextUtil

import scala.jdk.CollectionConverters.*

object SpringMain {

  def main(args: Array[String]): Unit = {

    val annotationConfigApplicationContext = new AnnotationConfigApplicationContext
//    annotationConfigApplicationContext.getBeanFactory.registerSingleton("dataFrame", DataFrame)

    val scanner = new ClassPathScanningCandidateComponentProvider(false)
    scanner.addIncludeFilter(new AnnotationTypeFilter(classOf[Component]))
    val candidates = scanner.findCandidateComponents("zuk.**")

    candidates.asScala.foreach(bd=>{
      val beanClassName =  bd.getBeanClassName
      val clazz = Class.forName(beanClassName)
      annotationConfigApplicationContext.registerBeanDefinition(clazz.getSimpleName, bd)
    })

    val source = new ResourcePropertySource("file:/d:/development/github/llmweb/web/boot/src/main/resources/application.properties")
    val env = annotationConfigApplicationContext.getEnvironment
    env.getPropertySources.addLast(source)

    annotationConfigApplicationContext.refresh()
    SpringContextUtil.context = annotationConfigApplicationContext

    val applicationProperties = SpringContextUtil.context.getBean(classOf[ApplicationProperties])
//    applicationProperties.init()
  }

}
