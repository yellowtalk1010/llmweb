package zuk.token.providers.tasks

import java.util.UUID
import scala.beans.BeanProperty

trait ITask {

  @BeanProperty var id: String = null
  @BeanProperty var chatContent: String = null

  def createPrompt(): String

}
