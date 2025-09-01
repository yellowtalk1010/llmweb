package zuk.sast.rules.controller.stock.analysis;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class LoaderStockData implements InitializingBean {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    public static final String TOKEN = "36c92182f783f08005017f78e7a264608a82952f8b91de2a";

    public static final String STOCK_DATA_DIR_PATH = "D:\\development\\github\\llmweb1\\web\\stocks";
    public static final String STOCK_TOKEN = STOCK_DATA_DIR_PATH + File.separator + "token";
    public static final String STOCK_ALL = STOCK_DATA_DIR_PATH + File.separator + "all_stock.json";
    public static final String STOCK_DAY = STOCK_DATA_DIR_PATH + File.separator + "days";



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

    public void load() throws Exception {
        File tokenFile = new File(STOCK_TOKEN);
        if(tokenFile.exists() && tokenFile.isFile() && FileUtils.readFileToString(tokenFile, "UTF-8").trim().equals(TOKEN)){
            loadAllStocks();
            EXECUTOR_SERVICE.execute(new ThreadDownloadStockDay());
        }
        else {
            log.info("STOCK_PATH路径错误");
            System.exit(0);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.load();
    }


    @Data
    public static class StockApiVO {
        private String api_code;
        private String jys;
        private String name;
        private String gl;
    }


}
