package zuk.sast.rules.controller.stock;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zuk.sast.rules.controller.stock.analysis.LoaderStockData;
import zuk.sast.rules.utils.HttpClientUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("stockBidding")
public class StockBiddingController {

    /***
     * 竞价强筹
     * @param period 时期，抢筹类型，0-竞价抢筹，1-尾盘抢筹
     * @param type 1委托金额排序  2成交金额排序  3开盘金额排序 4涨幅排序
     * @return
     *
     */
    private static final Map<String, JJQCResponse> JJQC_RESPONSE_MAP = new ConcurrentHashMap<>();
    @GetMapping("jjqc")
    public synchronized JJQCResponse jjqc(String period, String type) {
        String key = period + "_" + type;
        if (JJQC_RESPONSE_MAP.get(key)!=null) {
            return JJQC_RESPONSE_MAP.get(key);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tradeDate = sdf.format(new Date());
        String url = "https://stockapi.com.cn/v1/base/jjqc?period=" + period + "&type=" + type + "&tradeDate=" + tradeDate + "&token=" + LoaderStockData.TOKEN;
        System.out.println(url);
        try {
            String string = HttpClientUtil.sendGetRequest(url);
            JJQCResponse response = JSONObject.parseObject(string, JJQCResponse.class);
            response.getData().stream().forEach(e->{
                List<LoaderStockData.StockApiVO> stockApiVOList = LoaderStockData.STOCKS.stream().filter(stock->stock.getApi_code().equals(e.getCode())).toList();
                if(stockApiVOList!=null && stockApiVOList.size()>0){
                    e.setJys(stockApiVOList.get(0).getJys());
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
        private List<JJQCData> data;
    }

    @Data
    private static class JJQCData{
        private String jys; //SZ，SH
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
