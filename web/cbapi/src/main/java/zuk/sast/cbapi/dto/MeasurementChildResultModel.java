package zuk.sast.cbapi.dto;


import java.util.List;

public class MeasurementChildResultModel {
    private String describe;
    private String meatype;
    private String projectid;
    private String path;
    private String filename;
    private Integer startlinenumber;
    private Integer endlinenumber;
    private Integer nodelength;
    private Integer nodeoffset;
    private List<MeasurementChildResultModel> children;

    public MeasurementChildResultModel() {
    }

    public MeasurementChildResultModel(String describe, String meatype, String projectid, String path, String filename, Integer startlinenumber, Integer endlinenumber, Integer nodelength, Integer nodeoffset, List<MeasurementChildResultModel> children) {
        this.describe = describe;
        this.meatype = meatype;
        this.projectid = projectid;
        this.path = path;
        this.filename = filename;
        this.startlinenumber = startlinenumber;
        this.endlinenumber = endlinenumber;
        this.nodelength = nodelength;
        this.nodeoffset = nodeoffset;
        this.children = children;
    }

    public String getDescribe() {
        return this.describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getMeatype() {
        return this.meatype;
    }

    public void setMeatype(String meatype) {
        this.meatype = meatype;
    }

    public String getProjectid() {
        return this.projectid;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getStartlinenumber() {
        return this.startlinenumber;
    }

    public void setStartlinenumber(Integer startlinenumber) {
        this.startlinenumber = startlinenumber;
    }

    public Integer getEndlinenumber() {
        return this.endlinenumber;
    }

    public void setEndlinenumber(Integer endlinenumber) {
        this.endlinenumber = endlinenumber;
    }

    public Integer getNodelength() {
        return this.nodelength;
    }

    public void setNodelength(Integer nodelength) {
        this.nodelength = nodelength;
    }

    public Integer getNodeoffset() {
        return this.nodeoffset;
    }

    public void setNodeoffset(Integer nodeoffset) {
        this.nodeoffset = nodeoffset;
    }

    public List<MeasurementChildResultModel> getChildren() {
        return this.children;
    }

    public void setChildren(List<MeasurementChildResultModel> children) {
        this.children = children;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String describe;
        private String meatype;
        private String projectid;
        private String path;
        private String filename;
        private Integer startlinenumber;
        private Integer endlinenumber;
        private Integer nodelength;
        private Integer nodeoffset;
        private List<MeasurementChildResultModel> children;

        private Builder() {
        }

        public Builder describe(String describe) {
            this.describe = describe;
            return this;
        }

        public Builder meatype(String meatype) {
            this.meatype = meatype;
            return this;
        }

        public Builder projectid(String projectid) {
            this.projectid = projectid;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder startlinenumber(Integer startlinenumber) {
            this.startlinenumber = startlinenumber;
            return this;
        }

        public Builder endlinenumber(Integer endlinenumber) {
            this.endlinenumber = endlinenumber;
            return this;
        }

        public Builder nodelength(Integer nodelength) {
            this.nodelength = nodelength;
            return this;
        }

        public Builder nodeoffset(Integer nodeoffset) {
            this.nodeoffset = nodeoffset;
            return this;
        }

        public Builder children(List<MeasurementChildResultModel> children) {
            this.children = children;
            return this;
        }

        public MeasurementChildResultModel build() {
            return new MeasurementChildResultModel(this.describe, this.meatype, this.projectid, this.path, this.filename, this.startlinenumber, this.endlinenumber, this.nodelength, this.nodeoffset, this.children);
        }
    }
}