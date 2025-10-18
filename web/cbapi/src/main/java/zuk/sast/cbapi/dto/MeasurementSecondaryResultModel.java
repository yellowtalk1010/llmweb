package zuk.sast.cbapi.dto;


import java.util.List;

public class MeasurementSecondaryResultModel {
    private String filename;
    private String funname;
    private String classname;
    private String per;
    private String line;
    private String projectid;
    private String desc;
    private List<MeasurementChildResultModel> track;

    public MeasurementSecondaryResultModel() {
    }

    public MeasurementSecondaryResultModel(String filename, String funname, String classname, String per, String line, String projectid, String desc, List<MeasurementChildResultModel> track) {
        this.filename = filename;
        this.funname = funname;
        this.classname = classname;
        this.per = per;
        this.line = line;
        this.projectid = projectid;
        this.desc = desc;
        this.track = track;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFunname() {
        return this.funname;
    }

    public void setFunname(String funname) {
        this.funname = funname;
    }

    public String getClassname() {
        return this.classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getPer() {
        return this.per;
    }

    public void setPer(String per) {
        this.per = per;
    }

    public String getLine() {
        return this.line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getProjectid() {
        return this.projectid;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<MeasurementChildResultModel> getTrack() {
        return this.track;
    }

    public void setTrack(List<MeasurementChildResultModel> track) {
        this.track = track;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String filename;
        private String funname;
        private String classname;
        private String per;
        private String line;
        private String projectid;
        private String desc;
        private List<MeasurementChildResultModel> track;

        private Builder() {
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder funname(String funname) {
            this.funname = funname;
            return this;
        }

        public Builder classname(String classname) {
            this.classname = classname;
            return this;
        }

        public Builder per(String per) {
            this.per = per;
            return this;
        }

        public Builder line(String line) {
            this.line = line;
            return this;
        }

        public Builder projectid(String projectid) {
            this.projectid = projectid;
            return this;
        }

        public Builder desc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder track(List<MeasurementChildResultModel> track) {
            this.track = track;
            return this;
        }

        public MeasurementSecondaryResultModel build() {
            return new MeasurementSecondaryResultModel(this.filename, this.funname, this.classname, this.per, this.line, this.projectid, this.desc, this.track);
        }
    }
}
