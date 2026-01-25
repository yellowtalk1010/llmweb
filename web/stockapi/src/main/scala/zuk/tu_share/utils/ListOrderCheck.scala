package zuk.tu_share.utils

object ListOrderCheck {

  /***
   * 递增
   */
  def isIncreasing(xs: List[Float]): Boolean =
    xs.sliding(2).forall {
      case List(a, b) => a <= b
      case _ => true
    }

  /***
   * 递减
   * @param xs
   * @return
   */
  def isDecreasing(xs: List[Float]): Boolean =
    xs.sliding(2).forall {
      case List(a, b) => a >= b
      case _ => true
    }

}

