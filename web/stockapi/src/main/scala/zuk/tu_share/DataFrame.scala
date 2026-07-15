package zuk.tu_share

import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.dto.{ModuleDay, TsStock}
import zuk.tu_share.utils.All_stocks_csv_file_Util

import java.io.{File, FileOutputStream, FileReader, InputStream}
import java.math
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
import java.util.Properties


object DataFrame {

  val properties = new Properties()
  val turnover = "turnover"
  val change = "change"
  val config_properties = "stock_config.properties"

  /**
   * STOCKS_MAP 中 Key 为 ts_code
   */
  val STOCKS_MAP= new mutable.HashMap[String, TsStock]()

  /***
   * 加载 all_stocks.csv 中的数据
   */
  @Deprecated
  private def load__all_Stocks_csv(all_stocks_path: String): List[TsStock] = {
    val all_stocks_file = new File(all_stocks_path)
    println(s"加载all_stocks.csv文件，路径：${all_stocks_file.getAbsolutePath}，${all_stocks_file.exists()}")
    if (!all_stocks_file.exists() || !all_stocks_file.isFile) {
      System.exit(1)
    }
    //将tushare的csv数据转成对象
    val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record => {
        //股票代码
        val stockCode = record.get("ts_code")
        val stockName = record.get("name")

        val tsStock = new TsStock(stockCode, stockName)
        tsStock.symbol = record.get("symbol")
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
        moduleDay.total_mv = record.get("total_mv")
        moduleDay.limit = record.get("limit")

        moduleDay
      }).toList
      in.close()
      moduleDays ++= ls
    }
    catch
      case exception: Exception => exception.printStackTrace()

    //按时间降序
    val sorted = moduleDays.sortBy(_.trade_date).reverse.toList
//      .filter(e=>{
//        //移除停牌股票，但是历史数据中，停牌数据不会出现
//        val tingPai = new BigDecimal(e.open).compareTo(math.BigDecimal.ZERO)==0
//          || new BigDecimal(e.high).compareTo(math.BigDecimal.ZERO)==0
//          || new BigDecimal(e.low).compareTo(math.BigDecimal.ZERO)==0
//          || new BigDecimal(e.close).compareTo(math.BigDecimal.ZERO)==0
//        !tingPai
//      })
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
    val files = rt_k_file.listFiles().filter(_.getName.endsWith(".csv")).sortBy(_.getName).reverse
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

    stockDayVoList.toList.filter(rtk=>{
      //移除停牌股票
      val tingPai = new BigDecimal(rtk.open).compareTo(math.BigDecimal.ZERO)==0
        || new BigDecimal(rtk.high).compareTo(math.BigDecimal.ZERO)==0
        || new BigDecimal(rtk.low).compareTo(math.BigDecimal.ZERO)==0
        || new BigDecimal(rtk.close).compareTo(math.BigDecimal.ZERO)==0
      !tingPai
    })
  }

  private def loadProperties() = {
    try{
      val configFile = new File(config_properties)
      println(s"加载stock_config.properties文件:${configFile.getAbsolutePath}, ${configFile.exists()}")
      if (!configFile.exists()) {
        val output = new FileOutputStream(config_properties)
        properties.put(turnover, "100")
        properties.put(change, "100")
        properties.store(output, "stock config")
        output.close()
      }
      properties.load(new FileReader(configFile))
      println(properties.toString)
    }
    catch
      case exception: Exception =>
  }

  /***
   * 加载待分析数据
   *
   * @param path 数据路径
   * @return map中的key是股票代码， list是组装的股票数据
   */
  def load(path: String): mutable.HashMap[String, List[ModuleDay]] = {

    loadProperties()

    //加载股票信息
    val all_stocks_path = path + File.separator + "all_stocks.csv"
    val allStocksFile = new File(all_stocks_path)
    println(s"加载all_stocks.csv文件路径:${allStocksFile.getAbsolutePath}，${allStocksFile.exists()}")
    if (!allStocksFile.exists()) {
      System.exit(1)
    }

    val stocks = All_stocks_csv_file_Util.load(allStocksFile.getAbsolutePath)

    stocks.foreach(e => {
      //转成MAP格式
      STOCKS_MAP.put(e.ts_code, e)
    })

    //加载实时日K
    val rt_k_path = path + File.separator + "rt_k"
    val rtks = loadRTK(rt_k_path)

    val dayMap = new mutable.HashMap[String, List[ModuleDay]]
    var count = 0
    if(rtks.isEmpty){
      println("没有计算rt_k")
      stocks.foreach(stock=>{
        try{
          val historyDays = loadModules(path, stock.ts_code)
          dayMap.put(stock.ts_code, historyDays)
          count = count + 1
          println(s"st:${count}/${stocks.size}")
        }
        catch
          case exception: Exception => exception.printStackTrace()
      })
    }
    else {

      //比较股票的名称
      rtks.foreach(rtk=>{
        try {
          val v = STOCKS_MAP.get(rtk.ts_code)
          if (v.isEmpty) {
            //股票中不存在
            println(s"${rtk.ts_code}, ${rtk.name} 在 ${all_stocks_path} 中不存在")
          }
          else {
            if (!v.get.name.replace(" ","").equals(rtk.name.replace(" ",""))) {
              //股票名称不一致
              println(s"${rtk.ts_code}名称将【${v.get.name.trim}】改为【${rtk.name.trim}】在${all_stocks_path} ")
              v.get.name = rtk.name.replace(" ","")
            }
            else {
              //去掉空格是一致的
            }
          }
        }
        catch
          case exception: Exception =>
      })

      //加载模型数据
      rtks.foreach(rtk => {
        try {
          val historyDays = loadModules(path, rtk.ts_code)
          if (historyDays != null && historyDays.size > 0) {

            val preTradeDay0 = historyDays.head //上一个交易日信息

            // 计算换手率
            val turnover_rate = new BigDecimal(rtk.vol)
              .divide(new BigDecimal(preTradeDay0.float_share)
                .multiply(new BigDecimal(properties.getProperty("turnover","100").toFloat)), 4, RoundingMode.DOWN)
            rtk.turnover_rate = turnover_rate.toString

            //计算涨跌幅
            val change =((new BigDecimal(rtk.close).subtract(new BigDecimal(rtk.pre_close)))
              .multiply(new BigDecimal(properties.getProperty("change", "100").toFloat)))
              .divide(new BigDecimal(rtk.pre_close), 4, RoundingMode.UP)
            rtk.change = change.toString

            val vol = new BigDecimal(rtk.vol).divide(new BigDecimal(properties.getProperty("vol"))).setScale(2, RoundingMode.DOWN)
            rtk.vol = vol.toString

            println(s"${rtk.ts_code}, ${rtk.name},close:${rtk.close}, change:${rtk.change}, trunover:${rtk.turnover_rate}, vol:${rtk.vol}")

            dayMap.put(rtk.ts_code, List(rtk) ++ historyDays)
            count = count + 1
            println(s"rt:${count}/${rtks.size}")
          }
        } catch
          case exception: Exception => exception.printStackTrace()
      })
    }

    dayMap.filter(_._2.size>100)

  }

}
