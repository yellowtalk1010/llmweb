package zuk.sast.spring.controller.component

import org.springframework.beans.BeansException
import org.springframework.context.{ApplicationContext, ApplicationContextAware}
import org.springframework.stereotype.Component

object SpringContextUtil {
  var context: ApplicationContext = null
}

@Component
class SpringContextUtil extends ApplicationContextAware {

  override def setApplicationContext(applicationContext: ApplicationContext): Unit = {
    SpringContextUtil.context = applicationContext
  }

  @throws[BeansException]
  def getBean[T](name: String): T = {
    SpringContextUtil.context.getBean(name).asInstanceOf[T]
  }

  @throws[BeansException]
  def getBean[T](clz: Class[T]): T = {
    val result: T = SpringContextUtil.context.getBean(clz)
    result
  }

}
