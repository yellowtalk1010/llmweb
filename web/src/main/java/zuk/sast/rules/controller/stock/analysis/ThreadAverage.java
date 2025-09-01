package zuk.sast.rules.controller.stock.analysis;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.SymbolTable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import zuk.sast.rules.utils.DateUtil;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/***
 * 均线计算
 */
@Slf4j
public class ThreadAverage implements Runnable{

    private List<String> codes = new ArrayList<>();

    public ThreadAverage(List<String> codes) {
        if(codes!=null && codes.size()>0){
            this.codes.addAll(codes);
        }
    }

    @Override
    public void run() {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
            LocalDate today = LocalDate.now();

            this.codes.stream().forEach(code->{
                try {
                    List<ThreadDownloadStockDay.StockDayVo> stockDayVoList = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        //最多向前推3个月
                        LocalDate premonthDate = today.minusMonths(i);
                        String preMonth = formatter.format(premonthDate);
                        String path = LoaderStockData.STOCK_DAY + File.separator + code + File.separator + preMonth + ".jsonl";
                        File file = new File(path);
                        if(!file.exists()){
                            log.info(path + "，不存在");
                        }
                        else {
                            //读取文件中的数据
                            List<String> lines = FileUtils.readLines(file, "UTF-8");
                            //将文件行数据转成json对象
                            List<ThreadDownloadStockDay.StockDayVo> ls = lines.stream().map(line->{
                                ThreadDownloadStockDay.StockDayVo stockDayVo =  JSONObject.parseObject(line, ThreadDownloadStockDay.StockDayVo.class);
                                return stockDayVo;
                            }).toList();
                            stockDayVoList.addAll(ls);
                        }
                        //
//                        if(stockDayVoList.size()>=30){
//                            break;
//                        }
                    }

                    //按时间排序
                    List<ThreadDownloadStockDay.StockDayVo> sortedList = stockDayVoList.stream()
                            .sorted(Comparator.comparing(ThreadDownloadStockDay.StockDayVo::getTime).reversed()) //按时间倒序
                            .toList();

                    System.out.println();
                    for(int i=0; i<sortedList.size(); i++){
                        if(i+5 < stockDayVoList.size()
                                && i+10 < stockDayVoList.size()
                                && i+30 < stockDayVoList.size()
                        ){

                            ThreadDownloadStockDay.StockDayVo stockDayVo = sortedList.get(i);

                            String date = stockDayVo.getTime();


                            BigDecimal avg1 = daysAVG(Arrays.asList(stockDayVo), 1);

                            List<ThreadDownloadStockDay.StockDayVo> stockDayVoList5 = sortedList.subList(i, i+5);
                            BigDecimal avg5 = daysAVG(stockDayVoList5, 5);

                            List<ThreadDownloadStockDay.StockDayVo> stockDayVoList10 = sortedList.subList(i, i+10);
                            BigDecimal avg10 = daysAVG(stockDayVoList10, 10);

                            List<ThreadDownloadStockDay.StockDayVo> stockDayVoList30 = sortedList.subList(i, i+30);
                            BigDecimal avg30 = daysAVG(stockDayVoList30, 30);

                            System.out.println();
                        }

                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
            });

        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

    //计算日均线
    private BigDecimal dayAVG(ThreadDownloadStockDay.StockDayVo stockDayVo){
        BigDecimal amount = new BigDecimal(stockDayVo.getAmount()); //交易额
        BigDecimal volume = new BigDecimal(stockDayVo.getVolume()); //交易数
        BigDecimal avg = amount.divide(volume, 5, BigDecimal.ROUND_HALF_UP);
        return avg;
    }

    //5日均线
    private BigDecimal daysAVG(List<ThreadDownloadStockDay.StockDayVo> stockDayVoList, int type) throws Exception {
        BigDecimal amount = new BigDecimal(stockDayVoList.stream().mapToDouble(e -> Double.valueOf(e.getAmount())).sum());
        BigDecimal volume = new BigDecimal(stockDayVoList.stream().mapToDouble(e -> Double.valueOf(e.getVolume())).sum());
        BigDecimal avg = amount.divide(volume, 5, BigDecimal.ROUND_HALF_UP);
        return avg;
    }



}

