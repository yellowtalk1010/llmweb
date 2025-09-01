package zuk.sast.rules.controller.stock;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import zuk.sast.rules.utils.HttpClientUtil;
import zuk.sast.rules.utils.ResourceFileUtils;

import java.io.File;
import java.net.http.HttpClient;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class LoaderStockData implements InitializingBean {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final String TOKEN = "36c92182f783f08005017f78e7a264608a82952f8b91de2a";

    private static final String STOCK_DATA_DIR_PATH = "D:\\AAAAAAAAAAAAAAAAAAAA\\github\\llmweb1\\web\\stocks\\";
    private static final String STOCK_TOKEN = STOCK_DATA_DIR_PATH + File.separator + "token";
    private static final String STOCK_ALL = STOCK_DATA_DIR_PATH + File.separator + "all_stock.json";
    private static final String STOCK_DAY = STOCK_DATA_DIR_PATH + File.separator + "days";



    public static final List<StockApiVO> STOCKS = new ArrayList<>();

    private void loadAllStocks(){
        try {
            File file = new File(STOCK_ALL);
            String content = FileUtils.readFileToString(file, "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(content);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            jsonArray.forEach(item -> {
                StockApiVO stockApiVO = JSONObject.parseObject(JSONObject.toJSONString(item), StockApiVO.class);
                STOCKS.add(stockApiVO);
            });
            log.info("stock总数:" + STOCKS.size());
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        File tokenFile = new File(STOCK_TOKEN);
        if(tokenFile.exists() && tokenFile.isFile() && FileUtils.readFileToString(tokenFile, "UTF-8").trim().equals(TOKEN)){
            loadAllStocks();
            EXECUTOR_SERVICE.execute(new StockDayThread());
        }
        else {
            System.out.println("STOCK_PATH路径错误");
            System.exit(0);
        }
    }

    /***
     * 获取历史日线数据，按月统计
     */
    public static class StockDayThread implements Runnable {

        @Override
        public void run() {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            //String ym = sdf.format(new Date());
            final String ym = "202508";
            final int total = 21;
            String startTime = "2025-08-01";
            String endTime = "2025-08-31";
            STOCKS.stream().forEach(stockApiVO -> {
                String path = STOCK_DAY + File.separator + stockApiVO.getApi_code() + File.separator + ym + ".jsonl";
                try {
                    if(new File(path).exists()){
                        List<String> lines = FileUtils.readLines(new File(path), "UTF-8");
                        if(lines.size()!=total){
                            FileUtils.delete(new File(path));
                            log.info(path + "存在，但是数据总行数不对");
                        }
                        else {
//                            log.info(path + "相同");
                        }
                    }
                    else {
                        //不存在
                        String url = "https://stockapi.com.cn/v1/base/day?token=" + TOKEN + "&code="+stockApiVO.getApi_code()+"&startDate="+startTime+"&endDate="+endTime+"&calculationCycle=100";
                        String response = HttpClientUtil.sendGetRequest(url);
                        JSONArray jsonArray = (JSONArray) JSONObject.parseObject(response).get("data");
                        List<String> lines = jsonArray.stream().map(e->{
                            String line = JSONObject.toJSONString(e, JSONWriter.Feature.LargeObject);
                            return line;
                        }).toList();
                        if(lines.size()==total){
                            FileUtils.writeLines(new File(path), lines);
                            log.info(path + "， 下载成功");
                        }
                        else {
                            log.info(path + "， 下载失败");
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                    log.info(path + "， 失败");
                }
            });
        }
    }



    @Data
    public static class StockApiVO {
        private String api_code;
        private String jys;
        private String name;
        private String gl;
    }


}
