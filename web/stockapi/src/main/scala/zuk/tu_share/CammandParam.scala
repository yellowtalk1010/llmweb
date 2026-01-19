package zuk.tu_share

object CammandParam {
  val param = new CammandParam()
}

class CammandParam {
  var path: String = "."
  var pwd: String = ""
  var back: Boolean = false
  var back_step: Int = 80

  override def toString: String = {
    s"CammandParam=path:${path}, pwd:******, back: ${back}, back_step:${back_step}"
  }
}
