package zuk.tu_share.send

object ISendFactory {

  def list(): List[ISend] = {
    List(
      new Console,
      new Email,
      new JsonFile
    )
  }

}
