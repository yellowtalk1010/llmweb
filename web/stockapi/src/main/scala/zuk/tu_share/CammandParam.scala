package zuk.tu_share

object CammandParam {
  val param = new CammandParam()
}

class CammandParam {
  var path: String = "."
  var pwd: String = null

  override def toString: String = {
    s"CammandParam=path:${path}, pwd:******"
  }
}
