package zuk.sast.rules.controller.stock;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import zuk.sast.rules.utils.ResourceFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class LoaderStockData implements InitializingBean {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static final File STOCK_DATA_FILE = new File("D:\\AAAAAAAAAAAAAAAAAAAA\\github\\llmweb1\\web\\stocks\\token");
    private static final String TOKEN = "fdaljwkfksajfkda16f4e6wsa1f6we546f1w31fs65efw1f3s1f3we5fw1ef3s1a3";

    public static final List<Map<String, String>> STOCKS = new ArrayList<>();

    private void loadAllStocks(){
        try {
            File file = ResourceFileUtils.findFile(STOCK_DATA_FILE.getParent() + File.separator + "all_stock.json");
            String content = FileUtils.readFileToString(file, "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(content);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            jsonArray.forEach(item -> {
                Map<String, String> map = new HashMap<>();
                JSONObject obj = (JSONObject) item;
                map.put("api_code", obj.getString("api_code"));
                map.put("jys", obj.getString("jys"));
                map.put("gl", obj.getString("gl")); //所属板块
                map.put("name", obj.getString("name"));
                STOCKS.add(map);
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
        if(STOCK_DATA_FILE.exists() && STOCK_DATA_FILE.isFile() && FileUtils.readFileToString(STOCK_DATA_FILE, "UTF-8").equals(TOKEN)){
            loadAllStocks();
            EXECUTOR_SERVICE.execute(new StockDayThread());
        }
    }

    public static class StockDayThread implements Runnable {

        @Override
        public void run() {
            log.info(STOCKS.size()+"");
        }
    }

}
