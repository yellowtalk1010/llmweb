package zuk.sast.rules.controller.stock;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zuk.sast.rules.utils.HttpClientUtil;
import zuk.stockapi.LoaderLocalStockData;
import zuk.stockapi.StockApiVo;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("stockBidding")
public class StockBiddingController {


    private static final Map<String, JJQCResponse> JJQC_RESPONSE_MAP = new ConcurrentHashMap<>();

    @GetMapping("merge")
    public synchronized JJQCResponse merge(String period) {
        JJQCResponse jjqcResponse1 = JJQC_RESPONSE_MAP.get(period+"_1");
        JJQCResponse jjqcResponse2 = JJQC_RESPONSE_MAP.get(period+"_2");
        JJQCResponse jjqcResponse3 = JJQC_RESPONSE_MAP.get(period+"_3");
        JJQCResponse jjqcResponse4 = JJQC_RESPONSE_MAP.get(period+"_4");
        if(jjqcResponse1!=null
                && jjqcResponse2!=null
                && jjqcResponse3!=null
                && jjqcResponse4!=null
        ){
            List<String> list = new ArrayList<>();
            list.addAll(jjqcResponse1.getData().stream().map(e->e.getCode()).collect(Collectors.toSet()));
            list.addAll(jjqcResponse2.getData().stream().map(e->e.getCode()).collect(Collectors.toSet()));
            list.addAll(jjqcResponse3.getData().stream().map(e->e.getCode()).collect(Collectors.toSet()));
            list.addAll(jjqcResponse4.getData().stream().map(e->e.getCode()).collect(Collectors.toSet()));
            Map<String, Long> sorted = list.stream()
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // 按 value 降序
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,              // 处理 key 冲突（不会发生）
                            LinkedHashMap::new           // 保持有序
                    ));

//            sorted.entrySet().forEach(entry->{
//                System.out.println(entry.getKey() + ":" + entry.getValue());
//            });

            for (int i = 4; i > 0; i--) {
                final long ll = i;
                System.out.println("------------------------------重复" + i + "次");
                sorted.entrySet().stream().filter(entry->{
                    return entry.getValue()==ll;
                }).forEach(entry->{
                    System.out.println(entry.getKey());
                });
            }



            JJQCResponse jjqcResponse = new JJQCResponse();
            jjqcResponse.setCode("20000");
            jjqcResponse.setMsg("OKOKOK");
            return jjqcResponse;
        }
        else {

            log.info("合并数据不全");
            JJQCResponse jjqcResponse = new JJQCResponse();
            jjqcResponse.setCode("500");
            jjqcResponse.setMsg("合并数据不全");
            return jjqcResponse;
        }
    }

    /***
     * 竞价强筹
     * @param period 时期，抢筹类型，0-竞价抢筹，1-尾盘抢筹
     * @param type 1委托金额排序  2成交金额排序  3开盘金额排序 4涨幅排序
     * @return
     *
     */
    @GetMapping("jjqc")
    public synchronized JJQCResponse jjqc(String period, String type, String tradeDate) {

        if(tradeDate==null || StringUtils.isEmpty(tradeDate)){
            tradeDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }

        String key = period + "_" + type + "_" + tradeDate;
        if (JJQC_RESPONSE_MAP.get(key)!=null) {
            return JJQC_RESPONSE_MAP.get(key);
        }
        String url = "https://stockapi.com.cn/v1/base/jjqc?period=" + period + "&type=" + type + "&tradeDate=" + tradeDate + "&token=" + LoaderLocalStockData.TOKEN();
        System.out.println(url);
        try {
            String string = HttpClientUtil.sendGetRequest(url);
            JJQCResponse response = JSONObject.parseObject(string, JJQCResponse.class);
            response.getData().stream().forEach(e->{
                List<StockApiVo> stockApiVOList = LoaderLocalStockData.STOCKS().stream().filter(stock->stock.getApi_code().equals(e.getCode())).toList();
                if(stockApiVOList!=null && stockApiVOList.size()>0){
                    e.setJys(stockApiVOList.get(0).getJys());
                    e.setGl(stockApiVOList.get(0).getGl());
                }
            });
            if(response.getData()!=null && response.getData().size()>0){
                JJQC_RESPONSE_MAP.put(key, response);
            }
            return response;
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            JJQCResponse response = new JJQCResponse();
            response.setCode("500");
            response.setMsg(e.getMessage());
            return response;
        }
    }


    @Data
    private static class JJQCResponse{
        private String code;
        private String msg;
        private List<JJQCData> data = new ArrayList<>();
    }

    @Data
    private static class JJQCData{

        private Integer mergeTime = 1; //合并次数
        private String jys; //SZ，SH
        private String gl;  //行业分类

        //
        private String name; //个股名称
        private String code;    //个股代码
        private Double openAmt; //开盘金额
        private Double qczf; //抢筹涨幅
        private Double qccje; //抢筹成交额
        private Double qcwtje;  //抢筹委托金额
        private Integer type;   //
        private Integer period; //
        private String time;    //


//                      "name": "ST华通",
//                      "code": "002602",
//                      "openAmt": 24347925,
//                      "qczf": 1.03,
//                      "qccje": 19353600,
//                      "qcwtje": 26517043,
//                      "type": 1,
//                      "period": 0,
//                      "time": "2025-09-02"
    }


}
