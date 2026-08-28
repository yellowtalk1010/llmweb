package zuk

import org.scalatest.funsuite.AnyFunSuite

class KotlinDemoTest extends AnyFunSuite {
  test("kotlin测试"){
    import com.kotlin.example.AppKt
    println("a")
    AppKt.main()

    println(
      getClass.getClassLoader.getResource(
        "com/kotlin/example/UserService.class"
      )
    )

    import com.kotlin.example.UserService
    val service = new UserService
    val sayHello = service.hello("hl")
    println(sayHello)

  }
}
