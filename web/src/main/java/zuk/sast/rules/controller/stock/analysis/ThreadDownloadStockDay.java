package zuk.sast.rules.controller.stock.analysis;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import zuk.sast.rules.utils.HttpClientUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ThreadDownloadStockDay implements Runnable{

    private AtomicInteger num = new AtomicInteger(0);

    private Set<String> failStocks = new HashSet<>();

    public ThreadDownloadStockDay(){

    }

    @Override
    public void run() {
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        //String ym = sdf.format(new Date());
        while (LoaderStockData.STOCKS.size()!=num.get()){
            log.info("下载失败的个股信息：" + JSONObject.toJSONString(this.failStocks));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            final String ym = sdf.format(new Date());

            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
            String startTime = sdf1.format(new Date()) + "-01";

            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            String endTime = sdf2.format(new Date());

            System.out.println(ym+"\t"+startTime+"\t"+endTime);

            LoaderStockData.STOCKS.stream().forEach(stockApiVO -> {
                String path = LoaderStockData.STOCK_DAY + File.separator + stockApiVO.getApi_code() + File.separator + ym + ".jsonl";
                String url = "https://stockapi.com.cn/v1/base/day?token=" + LoaderStockData.TOKEN + "&code="+stockApiVO.getApi_code()+"&startDate="+startTime+"&endDate="+endTime+"&calculationCycle=100";
                try {

                    //不存在
                    String response = HttpClientUtil.sendGetRequest(url);
                    JSONObject jsonObject = JSONObject.parseObject(response);
                    Integer resCode = (Integer)jsonObject.get("code");
                    String resMsg = (String)jsonObject.get("msg");
                    if(resCode==20000 && resMsg.equals("success")){
                        JSONArray jsonArray = (JSONArray) JSONObject.parseObject(response).get("data");
                        List<String> lines = jsonArray.stream().map(e->{
                            String line = JSONObject.toJSONString(e, JSONWriter.Feature.LargeObject);
                            StockDayVo stockDayVo = JSONObject.parseObject(line, StockDayVo.class);
                            if(StringUtils.isNotEmpty(stockDayVo.getOpen()) //开盘价
                                    && StringUtils.isNotEmpty(stockDayVo.getTime()) //交易时间
                                    && StringUtils.isNotEmpty(stockDayVo.getCode()) //代码
                                    && StringUtils.isNotEmpty(stockDayVo.getAmount())   //交易总金额
                                    && StringUtils.isNotEmpty(stockDayVo.getChangeRatio())  //相比上次收盘价的涨跌比率
                                    && StringUtils.isNotEmpty(stockDayVo.getHigh()) //最高价
                                    && StringUtils.isNotEmpty(stockDayVo.getLow())  //最低价
                                    && StringUtils.isNotEmpty(stockDayVo.getTurnoverRatio())    //换手率
                                    && StringUtils.isNotEmpty(stockDayVo.getVolume())   //交易量
                                    && StringUtils.isNotEmpty(stockDayVo.getClose())    //收盘

                            ){
                                return JSONObject.toJSONString(stockDayVo, JSONWriter.Feature.LargeObject);
                            }
                            else {
                                log.error(line + "， 数据为空");
                                System.exit(1);
                                return "";
                            }

                        }).toList();
                        FileUtils.writeLines(new File(path), lines);
                        System.out.println(stockApiVO.getApi_code() + "，行数：" + lines.size() + "，" + "成功，" + num.get() + "/" + LoaderStockData.STOCKS.size());
                        num.incrementAndGet();

                        if(lines.size()==0){
                            System.out.println(stockApiVO.getApi_code() + "， 下载数据为空。（可能停牌）");
                        }
                    }
                    else {
                        failStocks.add(stockApiVO.getApi_code());
                        System.out.println(stockApiVO.getApi_code() + "，返回数据异常" + response);
                    }


                }catch (Exception e) {
                    failStocks.add(stockApiVO.getApi_code());
                    e.printStackTrace();
                    System.out.println(url + "\n" + path + "\n失败");
                }
            });
        }
        log.info("完成全部个股的新数据更新，" + LoaderStockData.STOCKS.size());
    }


    /***
     * {
     * "code":"000001.SZ",
     * "time":"2025-08-01",
     * "open":"12.24",
     * "turnoverRatio":"0.5215960590205436",
     * "amount":"1240239179",
     * "high":"12.33",
     * "low":"12.15",
     * "changeRatio":"0.40883074407194553", //换手率
     * "close":"12.28",                     //收盘价
     * "volume":"101218698"
     * }
     */

    @Data
    public static class StockDayVo {
        private String code;
        private String time;
        private String open;
        private String turnoverRatio;
        private String amount;
        private String high;
        private String low;
        private String changeRatio;
        private String close;
        private String volume;

    }
}
