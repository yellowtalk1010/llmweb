package zuk.run

import org.apache.commons.csv.CSVFormat
import zuk.tu_share.dto.{ModuleDay, TsStock}

import java.io.{File, FileReader}
import java.math.{BigDecimal, RoundingMode}
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.*
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

object DataFrame {

  /***
   * 加载全部股票基本数据
   */
  private def loadAllStocks(all_stocks_path: String): List[TsStock] = {
    val all_stocks_file = new File(all_stocks_path)
    println(s"${all_stocks_file.getAbsolutePath}，${all_stocks_file.exists()}")
    if (!all_stocks_file.exists() || !all_stocks_file.isFile) {
      System.exit(1)
    }
    //将tushare的csv数据转成对象
    val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record => {
        val tsStock = new TsStock()
        tsStock.ts_code = record.get("ts_code")
        tsStock.symbol = record.get("symbol")
        tsStock.name = record.get("name")
        tsStock.area = record.get("area")
        tsStock.industry = record.get("industry")
        tsStock.market = record.get("market")
        tsStock
      })
      .toList
    in.close()
    println(s"${codes.size}")

    codes
  }


  /***
   * 加载模型数据
   */
  private def loadModules(path: String, ts_code: String): List[ModuleDay] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMM")
    val today = LocalDate.now
    val num = new AtomicInteger(0)
    val moduleDays = new ListBuffer[ModuleDay]

    val ts_code_path = ts_code.replace(".", "_")
    val module_path = path + File.separator + "module" + File.separator + s"${ts_code_path}.csv"
    val module_file = new File(module_path)
    if(!module_file.exists()){
      //判断模型路径是否存在
      //println(s"${module_file.getAbsolutePath}，${module_file.exists()}")
      return List.empty
    }

    try {
      //读取文件中的数据
      val in = new FileReader(module_file.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      val ls: List[ModuleDay] = records.asScala.map(record => {

        val moduleDay = new ModuleDay()
        moduleDay.ts_code = record.get("ts_code")
        moduleDay.name = record.get("name")
        moduleDay.trade_date = record.get("trade_date")
        moduleDay.open = record.get("open")
        moduleDay.high = record.get("high")
        moduleDay.low = record.get("low")
        moduleDay.close = record.get("close")
        moduleDay.pre_close = record.get("pre_close")
        moduleDay.change = record.get("change")
        moduleDay.vol = record.get("vol")
        moduleDay.amount = record.get("amount")
        moduleDay.turnover_rate = record.get("turnover_rate")
        moduleDay.float_share = record.get("float_share")

        moduleDay
      }).toList
      in.close()
      moduleDays ++= ls
    }
    catch
      case exception: Exception => exception.printStackTrace()

    //按时间降序
    val sorted = moduleDays.sortBy(_.trade_date).reverse.toList
    sorted
  }

  /** *
   * 加载实时日线
   */
  private def loadRTK(rt_k_path: String): List[ModuleDay] = {
    val rt_k_file = new File(rt_k_path)
    if(!rt_k_file.exists()){
      println(s"${rt_k_file.getAbsolutePath}, ${rt_k_file.exists()}")
      return List.empty
    }
    val files = rt_k_file.listFiles().sortBy(_.getName).reverse
    if(files==null || files.size==0){
      println("rt_k文件为空")
      return List.empty
    }

    val sdf = new SimpleDateFormat("yyyyMMdd")
    val trade_date = sdf.format(new Date())

    val stockDayVoList = new ListBuffer[ModuleDay]
    try {
      //读取文件中的数据
      val in = new FileReader(files.head.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      val ls: List[ModuleDay] = records.asScala.map(record => {
        // ts_code	name	pre_close	high	open	low	close	vol	amount	num

        val moduleDay = new ModuleDay()
        moduleDay.ts_code = record.get("ts_code")
        moduleDay.name = record.get("name")
        moduleDay.trade_date = trade_date
        moduleDay.open = record.get("open")
        moduleDay.high = record.get("high")
        moduleDay.low = record.get("low")
        moduleDay.close = record.get("close")
        moduleDay.pre_close = record.get("pre_close")
        moduleDay.vol = record.get("vol")
        moduleDay.amount = record.get("amount")

        moduleDay

      }).toList
      in.close()
      stockDayVoList ++= ls
    }
    catch
      case exception: Exception => exception.printStackTrace()

    stockDayVoList.toList
  }

  def load(path: String): Unit = {

    //判断路径是否存在
    val dir = new File(path)
    println(s"${dir.getAbsolutePath}，${dir.exists()}")
    if (!dir.exists() || !dir.isDirectory) {
      System.exit(1)
    }

    //加载股票信息
    val all_stocks_path = path + File.separator + "all_stocks.csv"
    val stocks = loadAllStocks(all_stocks_path)

    //加载实时日K
    val rt_k_path = path + File.separator + "rt_k"
    val rtks = loadRTK(rt_k_path)

    val stockMap = new mutable.HashMap[String, List[ModuleDay]]
    if(rtks.isEmpty){
      println("没有计算rt_k")
      stocks.foreach(stock=>{
        try{
          val historyDays = loadModules(path, stock.ts_code)
          stockMap.put(stock.ts_code, historyDays)
        }
        catch
          case exception: Exception => exception.printStackTrace()
      })
    }
    else {
      //加载模型数据
      rtks.foreach(rtk => {
        try {
          val historyDays = loadModules(path, rtk.ts_code)
          if (historyDays != null && historyDays.size > 0) {

            val preTradeDay0 = historyDays.head //上一个交易日信息
            rtk.pre_close = preTradeDay0.close  //

            // 计算换手率
            val turnover_rate = new BigDecimal(rtk.vol).divide(new BigDecimal(preTradeDay0.float_share), 4, RoundingMode.DOWN)
            rtk.turnover_rate = turnover_rate.toString

            //计算涨跌幅
            val change = (new BigDecimal(rtk.close).subtract(new BigDecimal(rtk.pre_close))).divide(new BigDecimal(rtk.pre_close), 4, RoundingMode.DOWN)
            rtk.change = change.toString

            stockMap.put(rtk.ts_code, List(rtk) ++ historyDays)
          }
        } catch
          case exception: Exception => exception.printStackTrace()
      })
    }



    println()

  }

}
