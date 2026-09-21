package zuk

import org.springframework.context.annotation.*
import org.springframework.core.env.{ConfigurableEnvironment, PropertiesPropertySource, SystemEnvironmentPropertySource}
import org.springframework.core.io.support.ResourcePropertySource

import scala.jdk.CollectionConverters.*

@Configuration
@ComponentScan(Array("zuk"))
@PropertySource(Array(
  "file:/D:/development/github/llmweb/web/boot/src/main/resources/application.properties", 
  "file:/D:/development/github/tushare/111/gitee_stockapi/stock_config.properties"
))
class AppConfig {
  
}

object AppMain {
  def main(args: Array[String]): Unit = {
    //启动 Spring 容器，传入配置类
    val ctx = new AnnotationConfigApplicationContext(classOf[AppConfig])
    
    //输出环境信息
    val env = ctx.getEnvironment
    env.getPropertySources.asScala.foreach(ps=>{
      println(s"${ps.getName}, ${ps.getClass.getName}")
      
      ps match {
        case rps: ResourcePropertySource =>
          rps.getPropertyNames.foreach(n=>{
            val s = s"${n}=${rps.getProperty(n)}"
            println(s)
          })
        case pps: PropertiesPropertySource =>
          //println("")
        case seps: SystemEnvironmentPropertySource =>
          //println("")
        case _=> assert(false)
      }
    })
    
    
    ctx.getBeanDefinitionNames.foreach(println)
    // 2. 获取 Bean 并调用
//    val controller = ctx.getBean(classOf[UserController])
//    controller.show()
    Main.main(args)
    // 3. 关闭容器
    ctx.close()
  }
}