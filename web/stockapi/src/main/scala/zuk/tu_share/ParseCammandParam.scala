package zuk.tu_share

import java.io.File

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
          //回测数据必须发邮件
          ParseCammandParam.param.back = true
          ParseCammandParam.param.email = true
        case "-back_step" =>
          ParseCammandParam.param.back_step = args(i + 1).toInt
        case "-json" =>
          ParseCammandParam.param.json = true
        case "-wrate" =>
          ParseCammandParam.param.wrate = args(i + 1).toFloat
        case  "email" =>
          ParseCammandParam.param.email = true
        case _ =>
    }
  }
}

class ParseCammandParam {
  //股票分析系统路径
//  var path: String = "."
  var path: String = "D:\\development\\github\\tushare\\111\\gitee_stockapi"
  //密码
  var pwd: String = ""
  //是否执行回测
  var back: Boolean = false
  //回测80个交易日
  var back_step: Int = 80
  //是否输出json格式
  var json: Boolean = false
  //是否发送邮件
  var email: Boolean = false
  //回测涨幅: 1.0%
  var wrate: Float = 1.00

  var all_stocks_csv_file = ParseCammandParam.param.path + File.separator + "all_stocks.csv"
  var rtk_file: String = path + File.separator + "rt_k" + File.separator + "rt_k.csv"
  var MODEL_BACK_TEST_RESULT_file: String = path + File.separator + "MODEL_BACK_TEST_RESULT.txt"
  var stock_config_properties_file: String = path + File.separator + "stock_config.properties"
  var concept_dir: String = path + File.separator + "concept"
  var result_json_dir: String = path + File.separator + "result_json"
  

  override def toString: String = {
    s"CammandParam=path:${path}, pwd:******, back: ${back}, back_step:${back_step}, json:${json}, wrate:${wrate}"
  }
}
