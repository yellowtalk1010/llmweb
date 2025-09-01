package zuk.sast.rules.controller.stock.analysis;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import zuk.sast.rules.utils.HttpClientUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ThreadDownloadStockDay implements Runnable{

    private AtomicInteger num = new AtomicInteger(0);
    public ThreadDownloadStockDay(){

    }

    @Override
    public void run() {
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        //String ym = sdf.format(new Date());
        while (LoaderStockData.STOCKS.size()!=num.get()){
            final String ym = "202509";
            String startTime = "2025-09-01";
            String endTime = "2025-09-01";
            LoaderStockData.STOCKS.stream().forEach(stockApiVO -> {
                String path = LoaderStockData.STOCK_DAY + File.separator + stockApiVO.getApi_code() + File.separator + ym + ".jsonl";
                String url = "https://stockapi.com.cn/v1/base/day?token=" + LoaderStockData.TOKEN + "&code="+stockApiVO.getApi_code()+"&startDate="+startTime+"&endDate="+endTime+"&calculationCycle=100";
                try {
                    if(new File(path).exists()){
                        num.incrementAndGet(); //
                        List<String> lines = FileUtils.readLines(new File(path), "UTF-8");
                        if(lines.size()==0){
                            //FileUtils.delete(new File(path));
                            log.info(path + " 文件空数据");
                        }
                    }
                    else {
                        //不存在

                        String response = HttpClientUtil.sendGetRequest(url);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        Integer resCode = (Integer)jsonObject.get("code");
                        String resMsg = (String)jsonObject.get("msg");
                        if(resCode==20000 && resMsg.equals("success")){
                            JSONArray jsonArray = (JSONArray) JSONObject.parseObject(response).get("data");
                            List<String> lines = jsonArray.stream().map(e->{
                                String line = JSONObject.toJSONString(e, JSONWriter.Feature.LargeObject);
                                return line;
                            }).toList();
                            FileUtils.writeLines(new File(path), lines);
                            log.info(path + "， 新数据写入成功" + num.get());
                            num.incrementAndGet();

                            if(lines.size()==0){
                                log.info(url + "， 下载数据为空。（可能停牌）");
                            }
                        }
                        else {
                            log.info(url + "，返回数据异常" + response);
                        }

                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                    log.info(url + "\n" + path + "\n失败");
                }
            });
        }
        log.info("完成全部个股的新数据更新，" + LoaderStockData.STOCKS.size());
    }
}
