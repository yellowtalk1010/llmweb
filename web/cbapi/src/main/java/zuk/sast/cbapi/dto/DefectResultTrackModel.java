package zuk.sast.cbapi.dto;

import java.util.List;
import java.util.Objects;

public class DefectResultTrackModel {
    private String filePath;
    private String descript;
    private int line;
    private List<DefectResultTrackModel> children;

    public DefectResultTrackModel() {
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDescript() {
        return this.descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public int getLine() {
        return this.line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public List<DefectResultTrackModel> getChildren() {
        return this.children;
    }

    public void setChildren(List<DefectResultTrackModel> children) {
        this.children = children;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            DefectResultTrackModel that = (DefectResultTrackModel)o;
            return this.line == that.line && Objects.equals(this.filePath, that.filePath) && Objects.equals(this.descript, that.descript) && Objects.equals(this.children, that.children);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.filePath, this.descript, this.line, this.children});
    }
}
