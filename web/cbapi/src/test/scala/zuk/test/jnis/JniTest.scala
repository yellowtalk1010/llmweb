//package zuk.test.jni
//
//object JniTest {
//
//  // 1️⃣ 声明 native 方法（由 C++ 实现）
//  @native def process(input: String): String
//
//  // 2️⃣ 加载动态库（与 Java 相同）
//  System.loadLibrary("myjni")
//
//  // 3️⃣ 主方法入口
//  def main(args: Array[String]): Unit = {
//    val result = process("Hello from Scala")
//    println(result)
//  }
//
//}
