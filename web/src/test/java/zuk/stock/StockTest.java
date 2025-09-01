package zuk.stock;

import org.junit.jupiter.api.Test;
import zuk.sast.rules.controller.stock.analysis.LoaderStockData;
import zuk.sast.rules.controller.stock.analysis.ThreadMA;


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
        ThreadMA threadMA = new ThreadMA(LoaderStockData.STOCKS.stream().map(e->e.getApi_code()).toList());
//        ThreadAverage threadAverage = new ThreadAverage(Lists.list("002633"));
        threadMA.run();
    }

}
