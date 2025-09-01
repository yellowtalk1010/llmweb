package zuk.sast.rules.controller.stock.analysis;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/***
 * 均线计算
 */
@Slf4j
public class ThreadAverage implements Runnable{

    private List<String> codes;

    public ThreadAverage(List<String> codes) {
        this.codes = codes;
    }

    @Override
    public void run() {

    }


    /***
     * {
     * "code":"000001.SZ",
     * "time":"2025-08-01",
     * "open":"12.24",
     * "turnoverRatio":"0.5215960590205436",
     * "amount":"1240239179",
     * "high":"12.33",
     * "low":"12.15",
     * "changeRatio":"0.40883074407194553", //换手率
     * "close":"12.28",                     //收盘价
     * "volume":"101218698"
     * }
     */

    @Data
    public static class StockDayVo {
        private String code;
        private String time;
        private String open;
        private String turnoverRatio;
        private String amount;
        private String high;
        private String low;
        private String changeRatio;
        private String close;
        private String volume;

    }

}
