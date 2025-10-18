package zuk.sast.cbapi.dto;


public class CodeCloneResultModel {
    String fileNum1;
    String fileNum2;
    int lineStart1;
    int lineEnd1;
    int lineStart2;
    int lineEnd2;

    public String getFileNum1() {
        return this.fileNum1;
    }

    public void setFileNum1(String fileNum1) {
        this.fileNum1 = fileNum1;
    }

    public String getFileNum2() {
        return this.fileNum2;
    }

    public void setFileNum2(String fileNum2) {
        this.fileNum2 = fileNum2;
    }

    public int getLineStart1() {
        return this.lineStart1;
    }

    public void setLineStart1(int lineStart1) {
        this.lineStart1 = lineStart1;
    }

    public int getLineEnd1() {
        return this.lineEnd1;
    }

    public void setLineEnd1(int lineEnd1) {
        this.lineEnd1 = lineEnd1;
    }

    public int getLineStart2() {
        return this.lineStart2;
    }

    public void setLineStart2(int lineStart2) {
        this.lineStart2 = lineStart2;
    }

    public int getLineEnd2() {
        return this.lineEnd2;
    }

    public void setLineEnd2(int lineEnd2) {
        this.lineEnd2 = lineEnd2;
    }

}
