package zuk

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.{AnnotationConfigApplicationContext, ClassPathScanningCandidateComponentProvider}
import org.springframework.core.`type`.filter.AnnotationTypeFilter
import org.springframework.stereotype.Component
import zuk.tu_share.DataFrame
import zuk.utils.SpringContextUtil

import scala.jdk.CollectionConverters.*

object SpringMain {

  def main(args: Array[String]): Unit = {

    val annotationConfigApplicationContext = new AnnotationConfigApplicationContext
//    annotationConfigApplicationContext.getBeanFactory.registerSingleton("dataFrame", DataFrame)
    SpringContextUtil.context = annotationConfigApplicationContext
    val scanner = new ClassPathScanningCandidateComponentProvider(false)
    scanner.addIncludeFilter(new AnnotationTypeFilter(classOf[Component]))
    val candidates = scanner.findCandidateComponents("zuk.**")
    candidates.asScala.foreach(bd=>{
      val beanClassName =  bd.getBeanClassName
      println(beanClassName)
    })
  }

}
