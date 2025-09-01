package zuk.sast.rules.controller.stock.analysis;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import zuk.sast.rules.utils.HttpClientUtil;

import java.io.File;
import java.util.List;

@Slf4j
public class ThreadDownloadStockDay implements Runnable{

    @Override
    public void run() {
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        //String ym = sdf.format(new Date());
        final String ym = "202508";
        final int total = 21;
        String startTime = "2025-08-01";
        String endTime = "2025-08-31";
        LoaderStockData.STOCKS.stream().forEach(stockApiVO -> {
            String path = LoaderStockData.STOCK_DAY + File.separator + stockApiVO.getApi_code() + File.separator + ym + ".jsonl";
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
                    String url = "https://stockapi.com.cn/v1/base/day?token=" + LoaderStockData.TOKEN + "&code="+stockApiVO.getApi_code()+"&startDate="+startTime+"&endDate="+endTime+"&calculationCycle=100";
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
                        log.info(url + "， 下载失败");
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
