package zuk.token.providers

import java.util.UUID
import scala.beans.BeanProperty

trait ITask {

  @BeanProperty var id: String = UUID.randomUUID().toString.replaceAll("-", "")
  @BeanProperty val chatContent: String = null
  @BeanProperty var responseText: String = null
  @BeanProperty var parserText: String = null
  @BeanProperty var finished = null

  def parseProvider(): String

}
