package zuk.tu_share

object CammandParam {
  val param = new CammandParam()

  def parse(args: Array[String]): Unit = {
    for (i <- 0 until args.size) {
      val v = args(i).toLowerCase
      v match
        case "-path" =>
          CammandParam.param.path = args(i + 1)
        case "-pwd" =>
          CammandParam.param.pwd = args(i + 1)
        case "-back" =>
          CammandParam.param.back = true
        case "-back_step" =>
          CammandParam.param.back_step = args(i + 1).toInt
        case "-json" =>
          CammandParam.param.json = true
        case _ =>
    }
  }
}

class CammandParam {
  var path: String = "."
  var pwd: String = ""
  var back: Boolean = false
  var back_step: Int = 80
  var json: Boolean = false

  override def toString: String = {
    s"CammandParam=path:${path}, pwd:******, back: ${back}, back_step:${back_step}, json:${json}"
  }
}
