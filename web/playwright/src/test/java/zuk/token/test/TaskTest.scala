package zuk.token.test

import zuk.token.providers.tasks.ITask

class TaskTest extends ITask {

  override def createPrompt(): String = {
    this.chatContent
  }
}
