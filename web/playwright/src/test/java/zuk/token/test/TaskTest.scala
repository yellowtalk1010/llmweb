package zuk.token.test

import zuk.token.providers.ITask

class TaskTest extends ITask {

  /**   *
   * 将deepseek、gpt进行解析
   *
   * @return
   */
  override def parseProvider(): String = ""

  /** *
   * 检测结果
   *
   * @return
   */
  override def checkResult(): Boolean = true
}
