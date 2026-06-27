package zuk.token.providers

import java.util.UUID
import scala.beans.BeanProperty

trait ITask(@BeanProperty val chatContent: String) {

  @BeanProperty var id: String = UUID.randomUUID().toString.replaceAll("-", "")
  @BeanProperty var responseText: String = null
  @BeanProperty var parserText: String = null
  @BeanProperty var finished = null

  def parse(): String

}
