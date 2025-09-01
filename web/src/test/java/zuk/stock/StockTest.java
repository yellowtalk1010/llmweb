package zuk.stock;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import zuk.sast.rules.controller.stock.analysis.LoaderStockData;
import zuk.sast.rules.controller.stock.analysis.ThreadMA;

import java.math.BigDecimal;
import java.util.List;


public class StockTest {

    private void loadStockData() throws Exception {
        LoaderStockData.load();
    }

    /***
     * 计算 MA 值线
     * @throws Exception
     */
    @Test
    public void ma() throws Exception {
        loadStockData();
//        ThreadMA threadMA = new ThreadMA(LoaderStockData.STOCKS.stream().map(e->e.getApi_code()).toList());
        ThreadMA threadMA = new ThreadMA(Lists.list("002633"));
        threadMA.run();

        ThreadMA.MAP.entrySet().stream().filter(e->e.getValue().size()>5).forEach(e->{
            String code = e.getKey();
            List<ThreadMA.StockMaVo> ls = e.getValue();
            ThreadMA.StockMaVo ma0 = ls.get(0);
            ThreadMA.StockMaVo ma1 = ls.get(1);
            ThreadMA.StockMaVo ma2 = ls.get(2);

            //换手率
            Double changeRatio0 = Double.valueOf(ma0.getStockDayVo().getChangeRatio()); //换手率
            Double changeRatio1 = Double.valueOf(ma1.getStockDayVo().getChangeRatio());
            Double changeRatio2 = Double.valueOf(ma2.getStockDayVo().getChangeRatio());

            boolean isChangeRatio = changeRatio0 > 0.2 && changeRatio0 < 0.6
                    && changeRatio1 > 0.2 && changeRatio1 < 0.6
                    && changeRatio2 > 0.2 && changeRatio2 < 0.6
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

            boolean isPrice = md5_2 > md5_1 && md5_1 > md5_0
                    && avg2 > avg1 && avg1 > avg0
                    && close2 > close1 && close1 > close0
                    ;

            if(isChangeRatio && isPrice){
                System.out.println(code);
            }

        });
    }

}
