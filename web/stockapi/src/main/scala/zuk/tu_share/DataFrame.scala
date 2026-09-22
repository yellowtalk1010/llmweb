package zuk.tu_share

import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import zuk.tu_share.dto.{ModuleDay, TsStock}
import zuk.tu_share.utils.Dataset_all_stocks_csv_file

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
import java.util.concurrent.{ConcurrentHashMap, Executors}


object DataFrame {

  private val properties = new Properties()
  
  /**
   * STOCKS_MAP 中 Key 为 ts_code
   */
  val STOCKS_MAP = new ConcurrentHashMap[String, TsStock]()
  
  /***
   * 历史日线数据
   */
  var HISTORY_MAP = new ConcurrentHashMap[String, List[ModuleDay]]()

  /** *
   * rtk 实时日线数据
   */
  var RTK_MAP = new ConcurrentHashMap[String, ModuleDay]()
  
  
  var execute = Executors.newSingleThreadExecutor()
  execute.submit(new Runnable {
    override def run(): Unit = {
      while (true) {
        try {
          loadModelAnalysisDataSet
          //开始定时更新
          loadRTK_DataSet
          println(s"完成定时更新rtk数据:${RTK_MAP.size()}")
          Thread.sleep(1000 * 60 * 5) //每两分钟更新一次rtk
        }
        catch {
          case exception: Exception =>
        }
      }
    }
  })


  /***
   * 创建并获取 properties 数据
   * @param configProperties
   * @return
   */
  def getProperties(): Properties = {
    try{
      val configProperties: String = ParseCammandParam.param.engineInfo.stock_config_properties_file
      if(properties.size() == 0){
        val configFile = new File(configProperties)
        println(s"加载stock_config.properties文件:${configFile.getAbsolutePath}, ${configFile.exists()}")
        if (configFile.exists()) {
          properties.load(new FileReader(configFile))
          println(properties.toString)
        }
      }
      properties
    }
    catch
      case exception: Exception =>
        exception.printStackTrace()
        properties
  }

  /***
   * 保存到文件中
   */
  def storeProperties() = {
    var output: FileOutputStream = null
    try {
      val configProperties: String = ParseCammandParam.param.engineInfo.stock_config_properties_file
      println(s"保存properties路径：${configProperties}")
      import zuk.tu_share.DataFrame
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      val dateStr = sdf.format(new Date())
      output = new FileOutputStream(configProperties)
      DataFrame.getProperties().store(output, s"${dateStr} stock config") //保存到文件中，并输出注释
    }
    catch
      case exception: Exception =>
        println("保存properties路径")
    finally {
      if(output!=null){
        output.close()
      }
    }
  }
  
  def getDataForSelect(tsCode: String): List[ModuleDay] = {
    val ls1 = HISTORY_MAP.get(tsCode)
    val list = if(RTK_MAP.get(tsCode)!=null){
      List(RTK_MAP.get(tsCode)) ++ ls1
    }    
    else {
      ls1
    }
    list
  }

  /***
   * 加载模型分析数据集
   *
   * @param path 数据路径
   * @return map中的key是股票代码， list是组装的股票数据
   */
  def loadModelAnalysisDataSet: mutable.HashMap[String, List[ModuleDay]] = synchronized {

    getProperties()

    //CSV文件中加载股票信息
    if(STOCKS_MAP.size < 5000){
      Dataset_all_stocks_csv_file.load.foreach(e => {
        //转成MAP格式
        STOCKS_MAP.put(e.ts_code, e)
      })
    }
    
    if(HISTORY_MAP.size() < 5000){
      //加载历史日线数据
      STOCKS_MAP.values().asScala.foreach(stock=>{
        loadStockHistoryData(stock.ts_code)  
      })
    }
    
    if(RTK_MAP.size() < 5000){
      //加载RTK数据
      loadRTK_DataSet
    }


    val dayMap = new mutable.HashMap[String, List[ModuleDay]]
     
    if(RTK_MAP.size() == 0){
      
      println("没有计算rt_k")
      STOCKS_MAP.values.asScala.toList.filter(stock=>{
        //股票需要出现历史数据中
        val ls = HISTORY_MAP.get(stock.ts_code)
        ls !=null && ls.size > 0
      }).foreach(stock=>{
        val historyDays = loadStockHistoryData(stock.ts_code)
        dayMap.put(stock.ts_code, historyDays)
      })
    }
    else {

      println("有计算rt_k")
      
      STOCKS_MAP.values().asScala.toList.filter(stock=>{
        val ls = HISTORY_MAP.get(stock.ts_code)
        ls !=null && ls.size > 0
      }).filter(stock=>{
        RTK_MAP.get(stock.ts_code) != null
      }).foreach(stock=>{
        val rtk = RTK_MAP.get(stock.ts_code)
        val historyDays = loadStockHistoryData(stock.ts_code)
        dayMap.put(stock.ts_code, List(rtk) ++ historyDays)
      })
      
    }

    dayMap.filter(_._2.size>100) //只返回日线记录超过100的

  }


  /***
   * 加载股票的历史的预备模型数据
   *
   * @param
   */
  private def loadStockHistoryData(ts_code: String): List[ModuleDay] = synchronized {
    
    if(HISTORY_MAP.get(ts_code) != null){
      return HISTORY_MAP.get(ts_code)
    }
    
    val formatter = DateTimeFormatter.ofPattern("yyyyMM")
    val today = LocalDate.now
    val num = new AtomicInteger(0)
    val moduleDays = new ListBuffer[ModuleDay]

    val ts_code_path = ts_code.replace(".", "_")
    val module_path = ParseCammandParam.param.engineInfo.stock_module_dir + File.separator + s"${ts_code_path}.csv"
    val module_file = new File(module_path)
    println(s"加载股票${ts_code}，${Dataset_all_stocks_csv_file.getTsStock(ts_code).getOrElse(new TsStock()).name}，的预备数据:${module_file.getAbsolutePath}, ${module_file.exists()}")
    if(!module_file.exists()){
      //判断模型路径是否存在
      //println(s"${module_file.getAbsolutePath}，${module_file.exists()}")
      println("上市交易天数不足120天")
      HISTORY_MAP.put(ts_code, List.empty)
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
        moduleDay.turnover_rate = record.get("turnover_rate") //换手率 成交量/无限售流通股数
        moduleDay.float_share = record.get("float_share")     //流通股本 (最新)
        moduleDay.total_mv = record.get("total_mv")           //总市值 收盘价*总股本
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
    
    //保存
    HISTORY_MAP.put(ts_code, sorted)
    
    sorted
    
  }

  /** *
   * 加载实时日线
   */
  private def loadRTK_DataSet: Unit = synchronized {

    val rt_k_file = new File(ParseCammandParam.param.engineInfo.rtk_file)
    println(s"加载实时日线数据:${rt_k_file.getAbsolutePath}, ${rt_k_file.exists()}")
    if(!rt_k_file.exists()){
      println(s"${rt_k_file.getAbsolutePath}, ${rt_k_file.exists()}")
      return List.empty
    }

    val sdf = new SimpleDateFormat("yyyyMMdd")
    val trade_date = sdf.format(new Date())

    val stockDayVoList = new ListBuffer[ModuleDay]
    try {
      //读取文件中的数据
      val in = new FileReader(rt_k_file, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      val ls: List[ModuleDay] = records.asScala.map(record => {
        // ts_code	name	pre_close	high	open	low	close	vol	amount	num

        val rtk = new ModuleDay()
        rtk.ts_code = record.get("ts_code")
        rtk.name = record.get("name")
        rtk.trade_date = trade_date
        rtk.open = record.get("open")
        rtk.high = record.get("high")
        rtk.low = record.get("low")
        rtk.close = record.get("close")
        rtk.pre_close = record.get("pre_close")
        rtk.vol = record.get("vol")
        rtk.amount = record.get("amount")

        rtk

      }).toList
      in.close()
      stockDayVoList ++= ls
    }
    catch
      case exception: Exception => exception.printStackTrace()

    val rtkList = stockDayVoList.toList.filter(rtk=>{
      //移除停牌股票
      val tingPai = new BigDecimal(rtk.open).compareTo(math.BigDecimal.ZERO)==0
        || new BigDecimal(rtk.high).compareTo(math.BigDecimal.ZERO)==0
        || new BigDecimal(rtk.low).compareTo(math.BigDecimal.ZERO)==0
        || new BigDecimal(rtk.close).compareTo(math.BigDecimal.ZERO)==0
      !tingPai
    })

    rtkList.foreach(this.updateRTK)  //更新rtk的涨跌幅等数据，并写入RTK_MAP中
    
    println(s"完成RTK日线数据加载:${RTK_MAP.size()}")
    
    
  }


  /***
   * 因为RTK数据很多缺失，因此需要重新计算： 换手率， 涨跌幅， 交易量。然后写入到RTK_MAP 数据中
   * 
   * @param rtk
   */
  private def updateRTK(rtk: ModuleDay): Unit = {
    if(StringUtils.isBlank(rtk.turnover_rate)   //换手率
      || StringUtils.isBlank(rtk.change)        //涨跌幅 
      || StringUtils.isBlank(rtk.vol))          //交易量 
    { 
      try {
        val historyDays = loadStockHistoryData(rtk.ts_code)
        if (historyDays != null && historyDays.size > 0) {

          val preTradeDay0 = historyDays.head //上一个交易日信息

          // 计算换手率
          val turnover_rate = new BigDecimal(rtk.vol)
            .divide(new BigDecimal(preTradeDay0.float_share)
              .multiply(new BigDecimal(properties.getProperty("turnover", "100").toFloat)), 4, RoundingMode.DOWN)
          rtk.turnover_rate = turnover_rate.toString

          //计算涨跌幅
          val change = ((new BigDecimal(rtk.close).subtract(new BigDecimal(rtk.pre_close)))
            .multiply(new BigDecimal(properties.getProperty("change", "100").toFloat)))
            .divide(new BigDecimal(rtk.pre_close), 4, RoundingMode.UP)
          rtk.change = change.toString

          val vol = new BigDecimal(rtk.vol).divide(new BigDecimal(properties.getProperty("vol"))).setScale(2, RoundingMode.DOWN)
          rtk.vol = vol.toString

          println(s"完成rtk数据整理(换手率/涨跌幅/交易量)：${rtk.ts_code}, ${rtk.name},close:${rtk.close}, change:${rtk.change}, trunover:${rtk.turnover_rate}, vol:${rtk.vol}")
          
          RTK_MAP.put(rtk.ts_code, rtk)
        }
      } catch
        case exception: Exception => 
          exception.printStackTrace()
    }
  }

}
