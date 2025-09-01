package zuk.sast.rules.controller.stock;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/***
 *
 */
@Slf4j
@RestController
@RequestMapping("stock")
public class AllStockController {

//    public static final List<Map<String, String>> STOCKS = new ArrayList<>();



    //https://xtrade.newone.com.cn/market/json?funcno=30029&version=1&stock_code=300287&market=SZ&start=0&dayitems=1&cms-trace-id=fe98fa982fbf42ce88b268b841bd8a84.1756699944822.16897853520034
    //https://xtrade.newone.com.cn/market/json?funcno=20006&version=1&stock_code=300287&market=SZ&count=100&cms-trace-id=fe98fa982fbf42ce88b268b841bd8a84.1756699944822.16897853520033
    //https://xtrade.newone.com.cn/market/json?funcno=30029&version=1&stock_code=300287&market=SZ&start=0&dayitems=5&cms-trace-id=fe98fa982fbf42ce88b268b841bd8a84.1756700218269.16897853520262
    //https://xtrade.newone.com.cn/market/json?funcno=20044&version=1&stock_code=300287&market=SZ&count=1000&type=day&cms-trace-id=fe98fa982fbf42ce88b268b841bd8a84.1756700327368.16897853520352

    //https://stockapi.com.cn/v1/base/day?token=36c92182f783f08005017f78e7a264608a82952f8b91de2a&code=600004&endDate=2025-08-31&startDate=2025-08-01&calculationCycle=100  //历史日线
//    static {
//        try {
//            File file = ResourceFileUtils.findFile("stocks/all_stock.json");
//            String content = FileUtils.readFileToString(file, "UTF-8");
//            JSONObject jsonObject = JSONObject.parseObject(content);
//            JSONArray jsonArray = jsonObject.getJSONArray("data");
//            jsonArray.forEach(item -> {
//                Map<String, String> map = new HashMap<>();
//                JSONObject obj = (JSONObject) item;
//                map.put("api_code", obj.getString("api_code"));
//                map.put("jys", obj.getString("jys"));
//                map.put("gl", obj.getString("gl")); //所属板块
//                map.put("name", obj.getString("name"));
//                STOCKS.add(map);
//            });
//            log.info("stock总数:" + STOCKS.size());
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            log.error(e.getMessage());
//        }
//    }

    @GetMapping("delete")
    public synchronized Map<String, Object> delete(String api_code) {
        log.info("delete:" + api_code);

        Map<String, Object> result = new HashMap<>();
        try {

            Map<String, JSONObject> mymap = readAttention();

            mymap.remove(api_code);

            writeAttention(mymap);

            result.put("status", "ok");
            return result;

        }catch (Exception e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            result.put("status", e.getMessage());
        }

        return result;
    }

    private static final File resultFile = new File("data/mystock");
    public static Map<String, JSONObject> readAttention() throws Exception {
        log.info("resultFile:" + resultFile.getAbsolutePath() + ", exists:" + resultFile.exists());
        List<String> list = FileUtils.readLines(resultFile, "UTF-8");
        Map<String, JSONObject> mymap = new HashMap<>();
        list.stream().forEach(l->{
            JSONObject jsonObject = JSONObject.parseObject(l);
            mymap.put(jsonObject.getString("api_code"), jsonObject);
        });
        log.info("总数：" + mymap.size());
        return mymap;
    }

    public static boolean writeAttention(Map<String, JSONObject> mymap) throws Exception {
        //重新转成行数据
        List<String> newLines = mymap.entrySet().stream().map(entry->{
            return JSONObject.toJSONString(entry.getValue(), JSONWriter.Feature.LargeObject);
        }).toList();
        //重新写入文件中
        FileUtils.writeLines(resultFile,"UTF-8", newLines);
        return true;
    }

    @GetMapping("add")
    public synchronized Map<String, Object> add(String api_code) {
        log.info("add:" + api_code);
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, JSONObject> mymap = readAttention();
            if(mymap.get(api_code)!=null){
                //已经存在
                result.put("status", "已存在");
                return result;
            }
            else {
                LoaderStockData.STOCKS.stream().filter(stock->stock.get("api_code").equals(api_code)).forEach(socket->{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("api_code", socket.get("api_code"));
                    jsonObject.put("jys", socket.get("jys"));
                    jsonObject.put("name", socket.get("name"));
                    mymap.put(api_code, jsonObject);
                });

                writeAttention(mymap);

                result.put("status", "ok");
                return result;
            }


        }catch (Exception e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            result.put("status", e.getMessage());
        }

        return result;
    }

    /***
     * 我的关注
     * @return
     */
    @GetMapping("my")
    public Map<String, Object> my(){
        try {
            Map<String, Object> map = new HashMap<>();
            Set<String> sets = readAttention().entrySet().stream().map(e->e.getKey()).collect(Collectors.toSet());
            List<Map<String, String>> ls = LoaderStockData.STOCKS.stream().filter(stock->{
                return sets.contains(stock.get("api_code"));
            }).toList();

            map.put("stocks", ls);
            return map;
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return new HashMap<>();
    }

    @GetMapping("all")
    public Map<String, Object> all(String search){

        List<Map<String, String>> list;
        if(search!=null && search.length()>0){
            list = new ArrayList<>();
            List<String> splits = Arrays.stream(search.split("#")).filter(e->e!=null && e.trim().length()>0).toList();

            splits.stream().forEach(s->{
                List<Map<String, String>> ls = LoaderStockData.STOCKS.stream().filter(stock->{
                    return stock.get("api_code").toUpperCase().contains(s.toUpperCase())
                            || stock.get("jys").toUpperCase().contains(s.toUpperCase())
                            || stock.get("gl").toUpperCase().contains(s.toUpperCase())
                            || stock.get("name").toUpperCase().contains(s.toUpperCase());
                }).toList();
                list.addAll(ls);
            });
        }
        else {
            list = LoaderStockData.STOCKS;
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
