package zuk.sast.rules.controller.stock;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zuk.sast.rules.utils.ResourceFileUtils;

import java.io.File;
import java.util.*;

/***
 *
 */
@Slf4j
@RestController
@RequestMapping("stock")
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

    @GetMapping("all")
    public Map<String, Object> all(String search){

        List<Map<String, String>> list = null;
        if(search!=null && search.length()>0){
            list = STOCKS.stream().filter(stock->{
                return stock.get("api_code").contains(search)
                        || stock.get("jys").contains(search)
                        || stock.get("gl").contains(search)
                        || stock.get("name").contains(search);
            }).toList();
        }
        else {
            list = STOCKS;
        }

        List<String> blocks = list.stream()
                .flatMap(stock->{
                    String[] arr = stock.get("gl").split(",");
                    return Arrays.stream(arr);
                }).toList();

        Map<String, Object> map = new HashMap<>();
        map.put("stocks", list);
        map.put("blocks",blocks);
        return map;
    }

}
