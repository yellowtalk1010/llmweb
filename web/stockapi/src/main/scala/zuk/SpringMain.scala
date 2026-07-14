package zuk

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.{AnnotationConfigApplicationContext, ClassPathScanningCandidateComponentProvider}
import zuk.utils.SpringContextUtil

import scala.jdk.CollectionConverters.*

object SpringMain {

  def main(args: Array[String]): Unit = {
    SpringContextUtil.context = new AnnotationConfigApplicationContext
    val scanner = new ClassPathScanningCandidateComponentProvider(false)
    val candidates = scanner.findCandidateComponents("zuk.**")
    candidates.asScala.foreach(bd=>{
      val beanClassName =  bd.getBeanClassName
      println(beanClassName)
    })
  }

}
