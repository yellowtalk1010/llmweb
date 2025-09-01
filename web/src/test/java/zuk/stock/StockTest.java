package zuk.stock;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import zuk.sast.rules.controller.stock.analysis.LoaderStockData;
import zuk.sast.rules.controller.stock.analysis.ThreadAverage;


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
        ThreadAverage threadAverage = new ThreadAverage(LoaderStockData.STOCKS.stream().map(e->e.getApi_code()).toList());
//        ThreadAverage threadAverage = new ThreadAverage(Lists.list("002633"));
        threadAverage.run();
    }

}
