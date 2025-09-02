package zuk.sast.rules.controller.stock;


import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zuk.sast.rules.utils.HttpClientUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/***
 * 竞价
 */
@Slf4j
@RestController
@RequestMapping("stock")
public class BiddingController {

    /***
     * 竞价强筹
     * @param period 时期，抢筹类型，0-竞价抢筹，1-尾盘抢筹
     * @param type 1委托金额排序  2成交金额排序  3开盘金额排序 4涨幅排序
     * @return
     *
     */
    @GetMapping("jjqc")
    public synchronized JSONObject delete(String period, String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tradeDate = sdf.format(new Date());
        String url = "https://stockapi.com.cn/v1/base/jjqc?period=" + period + "&type=" + type + "&tradeDate=" + tradeDate;
        try {
            String string = HttpClientUtil.sendGetRequest(url);
            JSONObject jsonObject = JSONObject.parseObject(string);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 500);
            jsonObject.put("msg", e.getMessage());
            return jsonObject;
        }
    }

}
