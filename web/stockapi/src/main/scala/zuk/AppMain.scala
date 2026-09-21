package zuk

import org.springframework.context.annotation.*

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

    // 2. 获取 Bean 并调用
//    val controller = ctx.getBean(classOf[UserController])
//    controller.show()
    Main.main(args)
    // 3. 关闭容器
    ctx.close()
  }
}