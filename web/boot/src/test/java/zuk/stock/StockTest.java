package zuk.stock;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zuk.sast.rules.controller.stock.analysis.LoaderStockData;
import zuk.sast.rules.controller.stock.analysis.ThreadDownloadStockDay;


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

}
