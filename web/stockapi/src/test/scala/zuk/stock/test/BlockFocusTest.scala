package zuk.stock.test

import org.scalatest.funsuite.AnyFunSuite
import zuk.stockapi.LoaderLocalStockData
import scala.jdk.CollectionConverters.*

class BlockFocusTest extends AnyFunSuite {

  val codes =
    """
      |300620
      |301389
      |688109
      |605089
      |002202
      |002709
      |600699
      |300144
      |688799
      |002289
      |002463
      |600400
      |002436
      |603398
      |603843
      |002913
      |301391
      |002555
      |001215
      |603618
      |605287
      |002715
      |300131
      |688400
      |300577
      |300859
      |601138
      |688593
      |605289
      |688627
      |000610
      |002429
      |601869
      |600641
      |300331
      |688498
      |600162
      |688195
      |603199
      |000988
      |601615
      |688678
      |002276
      |""".stripMargin

  test("聚焦板块"){
    LoaderLocalStockData.loadToken()


    val sets = codes.split("\r\n").toSet //切割
    val stocks = LoaderLocalStockData.STOCKS.asScala.filter(e=>sets.contains(e.getApi_code))

    val blockList = stocks.flatMap(e=>{
      e.getApi_code
      e.getName
      e.getGl.split(",")
    }).groupBy(e=>e)
      .toList
      .sortBy(_._2.size)
      .reverse

    val blockCodes = blockList.flatMap(tp2=>{
      val blockName = tp2._1
      val blockNum = tp2._2.size
      val codes = stocks.filter(e=>e.getGl.contains(blockName)).map(_.getApi_code)
      println(s"${blockName}, ${blockNum}")
      println(codes.mkString(","))
      codes.map(code=>(code, blockName))
    }).groupBy(_._1)
      .toList.sortBy(_._2.size).reverse
      .map(_._1)


    println("======================")
    println(blockCodes.size)
    blockCodes.foreach(println)


  }

}
