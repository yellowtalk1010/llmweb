package zuk.stockapi;

import zuk.sast.rules.controller.stock.analysis.ThreadDownloadStockDay;

public class StockMaVo {

    private String time;

    private StockDayVo stockDayVo;

    private String avg;
    private String avg5;
    private String avg10;
    private String avg20;
    private String avg30;

    private String ma5; //过去5个交易日收盘价的平均值
    private String ma10;
    private String md20;
    private String ma30;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public StockDayVo getStockDayVo() {
        return stockDayVo;
    }

    public void setStockDayVo(StockDayVo stockDayVo) {
        this.stockDayVo = stockDayVo;
    }

    public String getAvg() {
        return avg;
    }

    public void setAvg(String avg) {
        this.avg = avg;
    }

    public String getAvg5() {
        return avg5;
    }

    public void setAvg5(String avg5) {
        this.avg5 = avg5;
    }

    public String getAvg10() {
        return avg10;
    }

    public void setAvg10(String avg10) {
        this.avg10 = avg10;
    }

    public String getAvg20() {
        return avg20;
    }

    public void setAvg20(String avg20) {
        this.avg20 = avg20;
    }

    public String getAvg30() {
        return avg30;
    }

    public void setAvg30(String avg30) {
        this.avg30 = avg30;
    }

    public String getMa5() {
        return ma5;
    }

    public void setMa5(String ma5) {
        this.ma5 = ma5;
    }

    public String getMa10() {
        return ma10;
    }

    public void setMa10(String ma10) {
        this.ma10 = ma10;
    }

    public String getMd20() {
        return md20;
    }

    public void setMd20(String md20) {
        this.md20 = md20;
    }

    public String getMa30() {
        return ma30;
    }

    public void setMa30(String ma30) {
        this.ma30 = ma30;
    }
}
