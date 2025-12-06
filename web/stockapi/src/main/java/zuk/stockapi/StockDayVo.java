package zuk.stockapi;

public class StockDayVo {

    private String code;            //股票代码
    private String name;            //股票名称
    private String time;            //交易日期
    private String open;            //开盘价
    private String turnoverRatio;   //换手率
    private String amount;          //交易金额
    private String high;            //最高价
    private String low;             //最低价
    private String changeRatio;     //涨跌
    private String close;           //收盘价
    private String volume;          //交易量
    private String pre_close;       //昨收价

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getTurnoverRatio() {
        return turnoverRatio;
    }

    public void setTurnoverRatio(String turnoverRatio) {
        this.turnoverRatio = turnoverRatio;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getChangeRatio() {
        return changeRatio;
    }

    public void setChangeRatio(String changeRatio) {
        this.changeRatio = changeRatio;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getPre_close() {
        return pre_close;
    }

    public void setPre_close(String pre_close) {
        this.pre_close = pre_close;
    }
}
