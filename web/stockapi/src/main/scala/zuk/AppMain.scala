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
    // 1. 启动 Spring 容器，传入配置类
    val ctx = new AnnotationConfigApplicationContext(classOf[AppConfig])
    val env = ctx.getEnvironment.asInstanceOf[ConfigurableEnvironment]
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
        case _=>

          println("")
      }
      
//      if(ps.isInstanceOf[PropertiesPropertySource]){
//        
//      }
//      else if(ps.isInstanceOf[ResourcePropertySource]){
//        val resourcePropertySource = ps.asInstanceOf[org.springframework.core.io.support.ResourcePropertySource]
//        val names = resourcePropertySource.getPropertyNames
//        names.map(n=>{
//          s"${n}=${resourcePropertySource.getProperty(n)}"
//        }).foreach(println)
//      }
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