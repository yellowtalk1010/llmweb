package zuk.sast.rules.controller.stock;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
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
    public synchronized Map<String, Object> all(String search){

        List<Map<String, String>> list;
        if(search!=null && search.length()>0){
            list = new ArrayList<>();
            List<String> splits = Arrays.stream(search.split("#")).filter(e->e!=null && e.trim().length()>0).toList();

            splits.stream().forEach(s->{
                List<Map<String, String>> ls = STOCKS.stream().filter(stock->{
                    return stock.get("api_code").toUpperCase().contains(s.toUpperCase())
                            || stock.get("jys").toUpperCase().contains(s.toUpperCase())
                            || stock.get("gl").toUpperCase().contains(s.toUpperCase())
                            || stock.get("name").toUpperCase().contains(s.toUpperCase());
                }).toList();
                list.addAll(ls);
            });
        }
        else {
            list = STOCKS;
        }

        List<String> blocks = Arrays.asList(
                "人工智能",
                "deepseek",
                "昇腾",
                "数据中心",
                "芯片"
        );

        log.info("search:" + search +  ", stocks:" + list.size() + ", blocks:" + blocks.size());

        Map<String, Object> map = new HashMap<>();
        map.put("stocks", list);
        map.put("blocks",blocks);
        return map;
    }

}
