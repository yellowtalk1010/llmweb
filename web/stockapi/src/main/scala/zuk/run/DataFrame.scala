package zuk.run

import org.apache.commons.csv.CSVFormat
import zuk.stockapi.{StockApiVo, StockDayVo}

import java.io.{File, FileReader}
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import java.math.{BigDecimal, RoundingMode}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.*

object DataFrame {

  /***
   * 加载全部股票基本数据
   */
  private def loadAllStocks(all_stocks_path: String) = {
    val all_stocks_file = new File(all_stocks_path)
    println(s"${all_stocks_file.getAbsolutePath}，${all_stocks_file.exists()}")
    if (!all_stocks_file.exists() || !all_stocks_file.isFile) {
      System.exit(1)
    }
    //将tushare的csv数据转成对象
    val in = new FileReader(all_stocks_file.getAbsolutePath, Charset.forName("UTF-8"))
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)

    val codes = records.asScala.map(record => {
        val ts_code = record.get("ts_code")
        val name = record.get("name")
        val splits = ts_code.split("\\.")//将数据中股票的代码拆分，如000001.SZ，拆分为000001，SZ
        val code = splits(0)
        val jys = splits(1)
        val gl = record.get("industry")
        val area = record.get("area")
        val stockApiVo = new StockApiVo()
        stockApiVo.setTs_code(ts_code)//tu_share股票代码
        stockApiVo.setApi_code(code)  //股票代码
        stockApiVo.setJys(jys)        //SZ, SH, BJ
        stockApiVo.setName(name)      //股票名称
        stockApiVo.setGl(gl)          //股票所属行业
        stockApiVo.setArea(area)      //股票所在区域
        stockApiVo
      })
      .toList
    in.close()
    println(s"${codes.size}")

    codes
  }


  /***
   * 加载模型数据
   */
  private def loadModules(path: String, ts_code: String): List[StockDayVo] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMM")
    val today = LocalDate.now
    val num = new AtomicInteger(0)
    val stockDayVoList = new ListBuffer[StockDayVo]

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
      val ls: List[StockDayVo] = records.asScala.map(record => {
        val ts_code = record.get("ts_code")
        val name = record.get("name")
        val trade_date = record.get("trade_date")
        val open = record.get("open")
        val turnover_rate = record.get("turnover_rate")
        val amount = record.get("amount")
        val high = record.get("high")
        val low = record.get("low")
        val change = record.get("change")
        val close = record.get("close")
        val vol = record.get("vol")
        val pre_close = record.get("pre_close")
        val float_share = record.get("float_share")

        val stockDayVo = new StockDayVo()
        stockDayVo.setCode(ts_code)
        stockDayVo.setName(name)
        stockDayVo.setTime(trade_date)
        stockDayVo.setOpen(open)
        stockDayVo.setTurnoverRatio(turnover_rate)
        stockDayVo.setAmount(amount)
        stockDayVo.setHigh(high)
        stockDayVo.setLow(low)
        stockDayVo.setChangeRatio(change)
        stockDayVo.setClose(close)
        stockDayVo.setVolume(vol)
        stockDayVo.setPre_close(pre_close)
        stockDayVo.setFloat_share(float_share)

        stockDayVo
      }).toList
      in.close()
      stockDayVoList ++= ls
    }
    catch
      case exception: Exception => exception.printStackTrace()

    //按时间降序
    val sorted = stockDayVoList.sortBy(_.getTime).reverse.toList
    sorted
  }

  /** *
   * 加载实时日线
   */
  private def loadRTK(rt_k_path: String): List[StockDayVo] = {
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

    val stockDayVoList = new ListBuffer[StockDayVo]
    try {
      //读取文件中的数据
      val in = new FileReader(files.head.getAbsolutePath, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      val ls: List[StockDayVo] = records.asScala.map(record => {
        // ts_code	name	pre_close	high	open	low	close	vol	amount	num
        val ts_code = record.get("ts_code")
        val name = record.get("name")
        val pre_close = record.get("pre_close")
        val high = record.get("high")
        val open = record.get("open")
        val low = record.get("low")
        val close = record.get("close")

        var vol = record.get("vol") //这里单位是股，需要转成成手
        vol = new BigDecimal(vol).divide(new BigDecimal(100), 4, RoundingMode.DOWN).toString

        var amount = record.get("amount")
        amount = new BigDecimal(amount).divide(new BigDecimal(1000), 4, RoundingMode.DOWN).toString

        val stockDayVo = new StockDayVo()
        stockDayVo.setCode(ts_code)
        stockDayVo.setName(name)
        stockDayVo.setTime(trade_date) //补充交易时间为运行时间
        stockDayVo.setOpen(open)
        stockDayVo.setAmount(amount)
        stockDayVo.setHigh(high)
        stockDayVo.setLow(low)
        stockDayVo.setClose(close)
        stockDayVo.setVolume(vol)
        stockDayVo.setPre_close(pre_close)

        stockDayVo
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

    val stockMap = new mutable.HashMap[String, List[StockDayVo]]

    val rtks = loadRTK(rt_k_path)

    if(rtks.isEmpty){
      println("没有计算rt_k")
      stocks.foreach(stock=>{
        try{
          val historyDays = loadModules(path, stock.getTs_code)
          stockMap.put(stock.getTs_code, historyDays)
        }
        catch
          case exception: Exception => exception.printStackTrace()
      })
    }
    else {
      //加载模型数据
      rtks.foreach(rtk => {
        try {
          val historyDays = loadModules(path, rtk.getCode)
          if (historyDays != null && historyDays.size > 0) {

            val preTradeDay0 = historyDays.head //上一个交易日信息
            rtk.setPre_close(preTradeDay0.getClose) //

            // 计算换手率
            val turnover_rate = new BigDecimal(rtk.getVolume).divide(new BigDecimal(preTradeDay0.getFloat_share), 4, RoundingMode.DOWN)
            rtk.setTurnoverRatio(turnover_rate.toString)

            //计算涨跌幅
            val change = (new BigDecimal(rtk.getClose).subtract(new BigDecimal(rtk.getPre_close))).divide(new BigDecimal(rtk.getPre_close), 4, RoundingMode.DOWN)
            rtk.setChangeRatio(change.toString)

            stockMap.put(rtk.getCode, List(rtk) ++ historyDays)
          }
        } catch
          case exception: Exception => exception.printStackTrace()
      })
    }



    println()

  }

}
