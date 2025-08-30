package zuk.sast.rules.controller.stock;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RestController;
import zuk.sast.rules.utils.ResourceFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 *
 */
@Slf4j
@RestController
public class AllStockController {

    public static final List<Map<String, String>> STOCKS = new ArrayList<>();

    static {
        try {
            File file = ResourceFileUtils.findFile("stocks/all_stock.json");
            String content = FileUtils.readFileToString(file, "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(content);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            jsonArray.forEach(item -> {
                Map<String, String> map = new HashMap<>();
                JSONObject obj = (JSONObject) item;
                map.put("api_code", obj.getString("api_code"));
                map.put("jys", obj.getString("jys"));
                map.put("gl", obj.getString("gl"));
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



}
