package zuk.stock;

import org.junit.jupiter.api.Test;
import zuk.sast.rules.controller.stock.analysis.LoaderStockData;
import zuk.sast.rules.controller.stock.analysis.ThreadAverage;


public class StockTest {

    private void loadStockData() throws Exception {
        LoaderStockData.load();
    }

    @Test
    public void test1() throws Exception {
        loadStockData();
        ThreadAverage threadAverage = new ThreadAverage(LoaderStockData.STOCKS.stream().map(e->e.getApi_code()).toList());
        threadAverage.run();
    }

}
