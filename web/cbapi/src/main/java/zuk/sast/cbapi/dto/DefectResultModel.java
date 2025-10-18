package zuk.sast.cbapi.dto;


import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DefectResultModel {
    private int line;
    private String descript;
    private String categoryId;
    private String categoryName;
    private String ruleName;
    private String ruleId;
    private int ruleCatagory;
    private Set<String> ruleTags;
    private int level;
    private String filepath;
    private String language;
    private String md5;
    private Integer delStartLineNum;
    private Integer delEndLineNum;
    private Integer addStartLineNum;
    private String addCode;
    private Set<String> toolTypes;
    private List<DefectResultTrackModel> children;

    public int getLine() {
        return this.line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getDescript() {
        return this.descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getRuleName() {
        return this.ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public int getRuleCatagory() {
        return this.ruleCatagory;
    }

    public void setRuleCatagory(int ruleCatagory) {
        this.ruleCatagory = ruleCatagory;
    }

    public Set<String> getRuleTags() {
        return this.ruleTags;
    }

    public void setRuleTags(Set<String> ruleTags) {
        this.ruleTags = ruleTags;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getFilepath() {
        return this.filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Integer getDelStartLineNum() {
        return this.delStartLineNum;
    }

    public void setDelStartLineNum(Integer delStartLineNum) {
        this.delStartLineNum = delStartLineNum;
    }

    public Integer getDelEndLineNum() {
        return this.delEndLineNum;
    }

    public void setDelEndLineNum(Integer delEndLineNum) {
        this.delEndLineNum = delEndLineNum;
    }

    public Integer getAddStartLineNum() {
        return this.addStartLineNum;
    }

    public void setAddStartLineNum(Integer addStartLineNum) {
        this.addStartLineNum = addStartLineNum;
    }

    public String getAddCode() {
        return this.addCode;
    }

    public void setAddCode(String addCode) {
        this.addCode = addCode;
    }

    public List<DefectResultTrackModel> getChildren() {
        return this.children;
    }

    public void setChildren(List<DefectResultTrackModel> children) {
        this.children = children;
    }

    public Set<String> getToolTypes() {
        return this.toolTypes;
    }

    public void setToolTypes(Set<String> toolTypes) {
        this.toolTypes = toolTypes;
    }

    public DefectResultModel() {
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            DefectResultModel that = (DefectResultModel)o;
            return this.line == that.line && this.ruleCatagory == that.ruleCatagory && this.level == that.level && Objects.equals(this.descript, that.descript) && Objects.equals(this.categoryId, that.categoryId) && Objects.equals(this.categoryName, that.categoryName) && Objects.equals(this.ruleName, that.ruleName) && Objects.equals(this.ruleId, that.ruleId) && Objects.equals(this.ruleTags, that.ruleTags) && Objects.equals(this.filepath, that.filepath) && Objects.equals(this.language, that.language) && Objects.equals(this.md5, that.md5) && Objects.equals(this.delStartLineNum, that.delStartLineNum) && Objects.equals(this.delEndLineNum, that.delEndLineNum) && Objects.equals(this.addStartLineNum, that.addStartLineNum) && Objects.equals(this.addCode, that.addCode) && Objects.equals(this.toolTypes, that.toolTypes) && Objects.equals(this.children, that.children);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.line, this.descript, this.categoryId, this.categoryName, this.ruleName, this.ruleId, this.ruleCatagory, this.ruleTags, this.level, this.filepath, this.language, this.md5, this.delStartLineNum, this.delEndLineNum, this.addStartLineNum, this.addCode, this.toolTypes, this.children});
    }
}
