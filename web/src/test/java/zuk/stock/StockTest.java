package zuk.stock;

import org.junit.jupiter.api.Test;
import zuk.sast.rules.controller.stock.analysis.LoaderStockData;


public class StockTest {

    private void loadStockData() throws Exception {
        LoaderStockData.load();
    }

    @Test
    public void test1() throws Exception {
        loadStockData();
    }

}
