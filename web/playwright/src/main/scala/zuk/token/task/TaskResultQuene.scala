package zuk.token.task

import java.util
import java.util.concurrent.{Executors, LinkedBlockingQueue}

object TaskResultQuene {

  val taskResultList = new LinkedBlockingQueue[TaskRes]()

  val executors = Executors.newSingleThreadExecutor()
  var num: Long = 0
  var count: Long = 0

  excute() //启动线程池

  def excute(): Unit = {
    executors.execute(()=>{
      while(true){
        try {
          Thread.sleep(500)
          val res: TaskRes = taskResultList.take()  //不阻塞
          if(res!=null){
            num = num + 1
            println(s"${num}======${TaskResultQuene.count}======================${res.taskId}, ${res.result}")
          }
        }
        catch {
          case exception: Exception => exception.printStackTrace()
        }
      }
    })
  }

}
