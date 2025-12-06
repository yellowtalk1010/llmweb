package zuk.tu_share.pass

object PassFactory {

  def passList(): List[IPass] = {
    List(new PassMA)
  }

}
