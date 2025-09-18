package zuk.stock;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zuk.sast.rules.controller.stock.analysis.LoaderStockData;
import zuk.sast.rules.controller.stock.analysis.ThreadDownloadStockDay;
import zuk.sast.rules.controller.stock.analysis.ThreadMA;

import java.math.BigDecimal;
import java.util.List;


public class StockTest {

    private static final Logger log = LoggerFactory.getLogger(StockTest.class);

    private void loadStockData() throws Exception {
        LoaderStockData.load();
    }

    /***
     * 下载每个交易日的个股信息
     * @throws Exception
     */
    @Test
    public void downloadStockDay() throws Exception {
        loadStockData();
        ThreadDownloadStockDay downloadStockDay = new ThreadDownloadStockDay();
        downloadStockDay.run();
    }

    /***
     * 计算 MA 值线
     * @throws Exception
     */
    @Test
    public void ma() throws Exception {
        loadStockData();
        List<String> codes = LoaderStockData.STOCKS.stream().filter(e->{
            String gl = e.getGl().toUpperCase();
            Boolean st = gl.contains("人工智能")
                    || gl.contains("华为")
                    || gl.contains("芯片")
                    || gl.contains("金融")
                    || gl.contains("DEEPSEEK")
                    || gl.contains("消费")
                    || gl.contains("昇腾")
                    || gl.contains("AI")
                    || gl.contains("稀土")
                    || gl.contains("工业母机")
                    ;
//            return true;
            return e.getApi_code().equals("600641");
        }).map(e->e.getApi_code()).toList();
        log.info("总数：" + codes.size());
        ThreadMA threadMA = new ThreadMA(codes);
//        ThreadMA threadMA = new ThreadMA(Lists.list("002633"));
        threadMA.run();

        ThreadMA.MAP.entrySet().stream().filter(e->e.getValue().size()>5).forEach(e->{
            String code = e.getKey();
            List<ThreadMA.StockMaVo> ls = e.getValue();
            ThreadMA.StockMaVo ma0 = ls.get(0);
            ThreadMA.StockMaVo ma1 = ls.get(1);
            ThreadMA.StockMaVo ma2 = ls.get(2);

            //换手率
            Double turnoverRatio0 = Double.valueOf(ma0.getStockDayVo().getTurnoverRatio());
            Double turnoverRatio1 = Double.valueOf(ma1.getStockDayVo().getTurnoverRatio());
            Double turnoverRatio2 = Double.valueOf(ma2.getStockDayVo().getTurnoverRatio());
            boolean isTurnoverRatio = turnoverRatio0 > 4 && turnoverRatio0 < 10
                    && turnoverRatio1 > 4 && turnoverRatio1 < 10
                    && turnoverRatio2 > 4 && turnoverRatio2 < 10
                    ;

            //涨跌幅
            Double changeRatio0 = Double.valueOf(ma0.getStockDayVo().getChangeRatio());
            Double changeRatio1 = Double.valueOf(ma1.getStockDayVo().getChangeRatio());
            Double changeRatio2 = Double.valueOf(ma2.getStockDayVo().getChangeRatio());

            boolean isChangeRatio = changeRatio0 > 2 && changeRatio0 < 30
                    && changeRatio1 > 2 && changeRatio1 < 30
                    && changeRatio2 > 2 && changeRatio2 < 30
                    ;

            //收盘价递增
            Double open0 = Double.valueOf(ma0.getStockDayVo().getOpen());
            Double open1 = Double.valueOf(ma1.getStockDayVo().getOpen());
            Double open2 = Double.valueOf(ma2.getStockDayVo().getOpen());

            Double close0 = Double.valueOf(ma0.getStockDayVo().getClose());
            Double close1 = Double.valueOf(ma1.getStockDayVo().getClose());
            Double close2 = Double.valueOf(ma2.getStockDayVo().getClose());

            Double avg0 = Double.valueOf(ma0.getAvg());
            Double avg1 = Double.valueOf(ma1.getAvg());
            Double avg2 = Double.valueOf(ma2.getAvg());

            Double md5_0 = Double.valueOf(ma0.getMa5());
            Double md5_1 = Double.valueOf(ma1.getMa5());
            Double md5_2 = Double.valueOf(ma2.getMa5());

            boolean isPrice = true
                    //md5_2 > md5_1 && md5_1 > md5_0
                    //&& avg2 > avg1 && avg1 > avg0
                   // && close2 > close1 && close1 > close0
                    ;

            if(isTurnoverRatio && isChangeRatio && isPrice){
                System.out.println(code);
            }

        });
    }

}
