package zuk.tu_share.pass

import zuk.tu_share.ParseCammandParam
import zuk.tu_share.dto.ModuleDay
import zuk.tu_share.module.*
import zuk.tu_share.send.ISendFactory

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object PassFactory {

  def moduleList(): List[IModel] = {
    var modules = List(
//      //        new MA1_Model,
      new MA1_1_Model, //最高胜率
//      //        new MA3_0_Model, //上穿MA5
//      //        new MA3_1_Model,
//      //        new MA3_2_Model, //反包两日阴线后继续下跌
//      //        new MA3_3_Model,
      new MA4_Model,
      new MA5_Model,
//      //        new MA6_Model,
      new MA7_Model,
      new MA8_Model
    )

    modules
  }

  private def passList(): List[IPass] = {
    List(
      new PassMA,
      new PassPriceLimit
    )
  }

  private def doPass(moduleDays: List[ModuleDay]) = {
    passList().foreach(pass=>{
      pass.handle(moduleDays)
    })
  }

  /***
   * 模型分析
   * @param map key是股票代码， list是分析数据
   * @param backtestLenght 回测数据长度
   * @return
   */
  def doModule(map: mutable.HashMap[String, List[ModuleDay]], backtestLenght: Int = 0) = {

    val finishModules = ListBuffer[IModel]()
    var count = 0
    map.foreach(e=>{
      val modules = moduleList()
      modules.foreach(module=>{
        try {
//          println(s"模型分析:${module.getClass.getSimpleName.toUpperCase}")
          val stock = e._1
          //
          val moduleDayList = e._2.slice(backtestLenght, e._2.size) //取前几个交易日的数据，用于回测
          if (ParseCammandParam.param.back) {
            //如果回测

            var startIndex = 0
            var isBreak = false
            val backTestStep = module.backTestStep //回测未来多少天的步长
            for (i <- backTestStep to 1 by -1){
              if(!isBreak){
                startIndex = backtestLenght - i
                if (startIndex < 0) {
                  //继续
                }
                else {
                  isBreak = true
                  //中断
                }
              }
            }

            if (startIndex < 0) {
              //只有当backtestLenght等于0时才会出现
              module.sells ++= List()
              module.buy = e._2(0)
            }
            else {
              module.sells ++= e._2.slice(startIndex, backtestLenght).reverse //连续两天
              module.buy = e._2(backtestLenght) //购买
            }

          }
          doPass(moduleDayList)
          module.run(moduleDayList)
          count = count + 1
          println(s"mod" +
            s":${ParseCammandParam.param.back.toString.substring(0,1)}" +  //是否回测，t是，f否
            s":${backtestLenght+1}/${ParseCammandParam.param.back_step}" + //回测进度
            s":${count}/${map.size * modules.size}")  //进度
        }
        catch
          case exception: Exception =>
            exception.printStackTrace()
      })
      finishModules ++= modules.filter(e=>e.getStockDto()!=null && e.getStockDto().tsStock!=null).sortBy(e=>(e.getStockDto().turnoverRate, e.getStockDto().tsStock.ts_code)).reverse
    })

    println(s"完成模型分析:${PassFactory.moduleList().map(_.getClass.getSimpleName.toUpperCase).mkString(", ")}")

    ISendFactory.doSend(finishModules.toList)

  }


}
