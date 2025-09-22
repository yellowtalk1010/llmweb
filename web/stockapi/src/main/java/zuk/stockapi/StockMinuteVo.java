package zuk.stockapi;

public class StockMinuteVo {

    private String time;
    private String price;
    private String shoushu; //手数
    private String danShu; //单数
    private String bsbz; //涨跌标志：1-跌，2-涨,4-竞价阶段

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getShoushu() {
        return shoushu;
    }

    public void setShoushu(String shoushu) {
        this.shoushu = shoushu;
    }

    public String getDanShu() {
        return danShu;
    }

    public void setDanShu(String danShu) {
        this.danShu = danShu;
    }

    public String getBsbz() {
        return bsbz;
    }

    public void setBsbz(String bsbz) {
        this.bsbz = bsbz;
    }
}
