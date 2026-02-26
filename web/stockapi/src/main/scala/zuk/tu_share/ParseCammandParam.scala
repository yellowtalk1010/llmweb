package zuk.tu_share

object ParseCammandParam {
  val param = new ParseCammandParam()

  def parse(args: Array[String]): Unit = {
    for (i <- 0 until args.size) {
      val v = args(i).toLowerCase
      v match
        case "-path" =>
          ParseCammandParam.param.path = args(i + 1)
        case "-pwd" =>
          ParseCammandParam.param.pwd = args(i + 1)
        case "-back" =>
          ParseCammandParam.param.back = true
        case "-back_step" =>
          ParseCammandParam.param.back_step = args(i + 1).toInt
        case "-json" =>
          ParseCammandParam.param.json = true
        case _ =>
    }
  }
}

class ParseCammandParam {
  var path: String = "."
  var pwd: String = ""
  var back: Boolean = false
  var back_step: Int = 80
  var json: Boolean = false

  override def toString: String = {
    s"CammandParam=path:${path}, pwd:******, back: ${back}, back_step:${back_step}, json:${json}"
  }
}
