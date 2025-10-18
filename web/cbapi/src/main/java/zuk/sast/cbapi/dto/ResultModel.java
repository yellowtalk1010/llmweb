package zuk.sast.cbapi.dto;


import java.util.ArrayList;
import java.util.List;

public class ResultModel {
    public List<DefectResultModel> defectResultModelList = new ArrayList();
    public List<MeasurementResultModel> measurementResultModellList = new ArrayList();
    public List<CodeCloneResultModel> codeCloneModelList = new ArrayList();
    public List<FunctionModel> functionModelList = new ArrayList();
    private String projectDir;

    public ResultModel() {
    }

    public String getProjectDir() {
        return this.projectDir;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = projectDir;
    }

    public List<CodeCloneResultModel> getCodeCloneModelList() {
        return this.codeCloneModelList;
    }

    public void setCodeCloneModelList(List<CodeCloneResultModel> codeCloneModelList) {
        this.codeCloneModelList = codeCloneModelList;
    }

    public List<DefectResultModel> getDefectResultModelList() {
        return this.defectResultModelList;
    }

    public void setDefectResultModelList(List<DefectResultModel> defectResultModelList) {
        this.defectResultModelList = defectResultModelList;
    }

    public List<MeasurementResultModel> getMeasurementResultModellList() {
        return this.measurementResultModellList;
    }

    public void setMeasurementResultModellList(List<MeasurementResultModel> measurementResultModellList) {
        this.measurementResultModellList = measurementResultModellList;
    }

    public List<FunctionModel> getFunctionModelList() {
        return this.functionModelList;
    }

    public void setFunctionModelList(List<FunctionModel> functionModelList) {
        this.functionModelList = functionModelList;
    }

    public void addDefectResultModelList(List<DefectResultModel> defectResultModelList) {
        this.defectResultModelList.addAll(defectResultModelList);
    }

    public void addMeasurementResultModellList(List<MeasurementResultModel> measurementResultModellList) {
        this.measurementResultModellList.addAll(measurementResultModellList);
    }

    public void addCodeCloneResultModelList(List<CodeCloneResultModel> codeCloneResultModelList) {
        this.codeCloneModelList.addAll(codeCloneResultModelList);
    }

    public void addFunctionModelList(List<FunctionModel> functionModelList) {
        this.functionModelList.addAll(functionModelList);
    }

    public boolean isEmpty() {
        return this.defectResultModelList.size() > 0 || this.measurementResultModellList.size() > 0 || this.codeCloneModelList.size() > 0;
    }

    public void addAll(ResultModel resultModel) {
        if (resultModel != null) {
            this.addDefectResultModelList(resultModel.getDefectResultModelList());
            this.addMeasurementResultModellList(resultModel.getMeasurementResultModellList());
            this.addCodeCloneResultModelList(resultModel.getCodeCloneModelList());
            this.addFunctionModelList(resultModel.getFunctionModelList());
        }

    }
}
