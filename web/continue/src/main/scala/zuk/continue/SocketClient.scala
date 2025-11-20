package zuk.continue

import zuk.continue.SocketBinary.socket

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

object SocketClient {

  def main(args: Array[String]): Unit = {
    val socket = new Socket("127.0.0.1", 3000)
    val socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream))
    var line = ""
    var times = 0
    while (true && times != 4){
      line = socketInput.readLine()
      if (line != null) {
        times = times + 1
        println(s"${times}，line:${line}")
      }
      Thread.sleep(2000)
    }
    println("over.")
  }

}
