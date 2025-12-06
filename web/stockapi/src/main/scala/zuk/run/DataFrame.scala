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

object DataFrame {

  /***
   * 加载全部股票基本数据
   */
  private def loadAllStocks(path: String) = {
    val all_stocks_path = path + File.separator + "all_stocks.csv"
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
        stockApiVo.setApi_code(code)  //股票代码
        stockApiVo.setName(name)      //股票名称
        stockApiVo.setJys(jys)        //SZ, SH, BJ
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
  private def loadModules(path: String, stock: StockApiVo): List[StockDayVo] = {
    val code = stock.getApi_code
    val formatter = DateTimeFormatter.ofPattern("yyyyMM")
    val today = LocalDate.now
    val num = new AtomicInteger(0)
    val stockDayVoList = new ListBuffer[StockDayVo]

    val module_path = path + File.separator + "module " + File.separator + s"${stock.getApi_code}_${stock.getJys}.csv"
    val module_file = new File(module_path)
    if(!module_file.exists()){
      //判断模型路径是否存在
      println(s"${module_file.getAbsolutePath}，${module_file.exists()}")
      return List.empty
    }

    try {
      //读取文件中的数据
      val in = new FileReader(path, Charset.forName("UTF-8"))
      val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in)
      val ls: List[StockDayVo] = records.asScala.map(record => {
        val ts_code = record.get("ts_code")
        val trade_date = record.get("trade_date")
        val open = record.get("open")
        val turnover_rate = record.get("turnover_rate")
        val amount = record.get("amount")
        val high = record.get("high")
        val low = record.get("low")
        val change = record.get("change")
        val close = record.get("close")
        val vol = record.get("vol")

        val stockDayVo = new StockDayVo()
        stockDayVo.setCode(ts_code)
        stockDayVo.setTime(trade_date)
        stockDayVo.setOpen(open)
        stockDayVo.setTurnoverRatio(turnover_rate)
        stockDayVo.setAmount(amount)
        stockDayVo.setHigh(high)
        stockDayVo.setLow(low)
        stockDayVo.setChangeRatio(change)
        stockDayVo.setClose(close)
        stockDayVo.setVolume(vol)

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
  private def loadRTK(path: String): List[StockDayVo] = {
    val rt_k_path = s"${path}" + File.separator + "rt_k"
    val rt_k_file = new File(rt_k_path)
    if(!rt_k_file.exists()){
      println(s"${rt_k_file.getAbsolutePath}, ${rt_k_file.exists()}")
      return List.empty
    }



    List.empty
  }

  def load(path: String): Unit = {

    //判断路径是否存在
    val dir = new File(path)
    println(s"${dir.getAbsolutePath}，${dir.exists()}")
    if (!dir.exists() || !dir.isDirectory) {
      System.exit(1)
    }

    val stocks = loadAllStocks(path)



  }

}
