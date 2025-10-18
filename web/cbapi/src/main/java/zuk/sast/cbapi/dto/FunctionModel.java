package zuk.sast.cbapi.dto;


import java.util.Set;

public class FunctionModel {
    private String functionName;
    private Set<Integer> line;
    private String filePath;

    public FunctionModel() {
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public Set<Integer> getLine() {
        return this.line;
    }

    public void setLine(Set<Integer> line) {
        this.line = line;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
