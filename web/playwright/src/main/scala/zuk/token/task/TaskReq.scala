package zuk.token.task

import java.util.UUID

@Deprecated
case class TaskReq(llmName: String, content: String, taskId: String) {

  def this(llmName: String, content: String) = {
    this(llmName, content, UUID.randomUUID().toString.replace("-", ""))
  }

}
