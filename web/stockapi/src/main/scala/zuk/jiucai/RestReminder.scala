package zuk.jiucai

import scala.beans.BeanProperty

import scala.jdk.CollectionConverters.*

class RestReminder {
  @BeanProperty val enabled = false
  @BeanProperty val hours = List(10, 11, 14, 16).asJava.toArray
}
