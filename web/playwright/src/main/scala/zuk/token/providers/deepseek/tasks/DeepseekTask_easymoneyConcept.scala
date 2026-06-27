package zuk.token.providers.deepseek.tasks

import zuk.token.providers.ITask

import scala.beans.BeanProperty

/***
 * @param chatContent
 */
class DeepseekTask_easymoneyConcept(@BeanProperty var content: String) extends ITask(content) {


}
