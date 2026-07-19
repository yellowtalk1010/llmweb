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
        case "-wrate" =>
          ParseCammandParam.param.wrate = args(i + 1).toFloat
        case _ =>
    }
  }
}

class ParseCammandParam {
  //股票分析系统路径
  var path: String = "."
  //密码
  var pwd: String = ""
  //是否执行回测
  var back: Boolean = false
  //回测80个交易日
  var back_step: Int = 80
  //是否输出json格式
  var json: Boolean = false
  //回测涨幅: 1.0%
  var wrate: Float = 1.00

  override def toString: String = {
    s"CammandParam=path:${path}, pwd:******, back: ${back}, back_step:${back_step}, json:${json}, wrate:${wrate}"
  }
}
