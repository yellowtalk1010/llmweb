package zuk.sast.spring.controller.component

import org.springframework.context.{ApplicationContext, ApplicationContextAware}
import org.springframework.stereotype.Component

object SpringApplicationUtil {
  var context: ApplicationContext = null
}

@Component
class SpringApplicationUtil extends ApplicationContextAware {

  override def setApplicationContext(applicationContext: ApplicationContext): Unit = {
    SpringApplicationUtil.context = applicationContext
  }

}
