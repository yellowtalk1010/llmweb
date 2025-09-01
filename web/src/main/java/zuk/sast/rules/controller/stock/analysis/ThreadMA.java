package zuk.sast.rules.controller.stock.analysis;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static zuk.sast.rules.controller.stock.analysis.LoaderStockData.STOCK_MA;

/***
 * 过去5、10、30个交易日的均值、收盘价的均值计算
 */
@Slf4j
public class ThreadMA implements Runnable{

    private List<String> codes = new ArrayList<>();

    @Getter
    private List<StockAverageVo> stockAverageVoList = new ArrayList<>();

    public ThreadMA(List<String> codes) {
        if(codes!=null && codes.size()>0){
            this.codes.addAll(codes);
        }
    }

    @Override
    public void run() {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
            final LocalDate today = LocalDate.now();
            AtomicInteger num = new AtomicInteger(0);
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

                    //按时间倒序排列
                    List<ThreadDownloadStockDay.StockDayVo> sortedList = stockDayVoList.stream()
                            .sorted(Comparator.comparing(ThreadDownloadStockDay.StockDayVo::getTime).reversed()) //按时间倒序
                            .toList();




                    //System.out.println();
                    for(int i=0; i<sortedList.size(); i++){
                        if(i+5 < stockDayVoList.size()
                                && i+10 < stockDayVoList.size()
                                && i+30 < stockDayVoList.size()
                        ){

                            ThreadDownloadStockDay.StockDayVo stockDayVo = sortedList.get(i);

                            String date = stockDayVo.getTime(); //日期


                            BigDecimal avg1 = daysAVG(Arrays.asList(stockDayVo), 1);    //今日均价

                            List<ThreadDownloadStockDay.StockDayVo> stockDayVoList5 = sortedList.subList(i, i+5);   //过去5天均价
                            BigDecimal avg5 = daysAVG(stockDayVoList5, 5);
                            BigDecimal ma5 = daysMA(stockDayVoList5, 5);

                            List<ThreadDownloadStockDay.StockDayVo> stockDayVoList10 = sortedList.subList(i, i+10); //过去10天均价
                            BigDecimal avg10 = daysAVG(stockDayVoList10, 10);
                            BigDecimal ma10 = daysMA(stockDayVoList10, 10);

                            List<ThreadDownloadStockDay.StockDayVo> stockDayVoList30 = sortedList.subList(i, i+30); //过去30天均价
                            BigDecimal avg30 = daysAVG(stockDayVoList30, 30);
                            BigDecimal ma30 = daysMA(stockDayVoList30, 30);


                            //System.out.println();
                            StockAverageVo stockAverageVo =new StockAverageVo();
                            stockAverageVo.setTime(date);
                            stockAverageVo.setStockDayVo(stockDayVo);
                            stockAverageVo.setAvg(avg1.toString());
                            stockAverageVo.setAvg5(avg5.toString());
                            stockAverageVo.setAvg10(avg10.toString());
                            stockAverageVo.setAvg30(avg30.toString());

                            stockAverageVo.setMa5(ma5.toString());
                            stockAverageVo.setMa10(ma10.toString());
                            stockAverageVo.setMa30(ma30.toString());

                            this.stockAverageVoList.add(stockAverageVo); //记录计算好的ma数据


                        }

                    }

                    List<String> maLines = this.stockAverageVoList.stream().map(e->{
                        String line = JSONObject.toJSONString(e, JSONWriter.Feature.LargeObject);
                        return line;
                    }).toList();
                    String maFile = STOCK_MA + File.separator + code + File.separator + formatter.format(today) + ".jsonl";
                    FileUtils.writeLines(new File(maFile), maLines);
                    log.info(num.incrementAndGet() + "/" + this.codes.size() + "，" + maFile + "，写入成功，");

                }
                catch (Exception e) {
                    e.printStackTrace();
                    log.info(code + "，写入失败，" + num.incrementAndGet() + "/" + this.codes.size());
                    log.error(e.getMessage());
                }
            });

        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

    //过去历史均价
    private BigDecimal daysAVG(List<ThreadDownloadStockDay.StockDayVo> stockDayVoList, int type) throws Exception {
        BigDecimal amount = new BigDecimal(stockDayVoList.stream().mapToDouble(e -> Double.valueOf(e.getAmount())).sum());
        BigDecimal volume = new BigDecimal(stockDayVoList.stream().mapToDouble(e -> Double.valueOf(e.getVolume())).sum());
        if(volume.compareTo(BigDecimal.ZERO)==0 || amount.compareTo(BigDecimal.ZERO)==0){
            return BigDecimal.ZERO;
        }
        else {
            BigDecimal avg = amount.divide(volume, 5, BigDecimal.ROUND_HALF_UP);
            return avg;
        }
    }

    //过去历史交易日的收盘价的平均值
    private BigDecimal daysMA(List<ThreadDownloadStockDay.StockDayVo> stockDayVoList, int type) throws Exception {
        BigDecimal totalClose = new BigDecimal(stockDayVoList.stream().mapToDouble(e->Double.valueOf(e.getClose())).sum());
        BigDecimal ma = totalClose.divide(new BigDecimal(stockDayVoList.size()), 5, BigDecimal.ROUND_HALF_UP);
        return ma;
    }


    @Data
    public static class StockAverageVo{
        private String time;

        private ThreadDownloadStockDay.StockDayVo stockDayVo;

        private String avg;
        private String avg5;
        private String avg10;
        private String avg30;

        private String ma5; //过去5个交易日收盘价的平均值
        private String ma10;
        private String ma30;
    }

}

