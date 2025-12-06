package zuk.stockapi;

public class StockApiVo {
    private String ts_code;
    private String api_code;
    private String jys;
    private String name;
    private String gl;      //
    private String area;    //区域，福建、海南

    public String getApi_code() {
        return api_code;
    }

    public void setApi_code(String api_code) {
        this.api_code = api_code;
    }

    public String getJys() {
        return jys;
    }

    public void setJys(String jys) {
        this.jys = jys;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGl() {
        return gl;
    }

    public void setGl(String gl) {
        this.gl = gl;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
