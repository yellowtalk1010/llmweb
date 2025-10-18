package zuk.sast.cbapi.dto;


import java.util.ArrayList;
import java.util.List;

public class MeasurementResultModel {
    private String name;
    private List<MeasurementSecondaryResultModel> secondaryResultModelList = new ArrayList();

    public MeasurementResultModel() {
    }

    public MeasurementResultModel(String name, List<MeasurementSecondaryResultModel> secondaryResultModelList) {
        this.name = name;
        this.secondaryResultModelList = secondaryResultModelList;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MeasurementSecondaryResultModel> getSecondaryResultModelList() {
        return this.secondaryResultModelList;
    }

    public void setSecondaryResultModelList(List<MeasurementSecondaryResultModel> secondaryResultModelList) {
        this.secondaryResultModelList = secondaryResultModelList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private List<MeasurementSecondaryResultModel> secondaryResultModelList;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder secondaryResultModelList(List<MeasurementSecondaryResultModel> secondaryResultModelList) {
            this.secondaryResultModelList = secondaryResultModelList;
            return this;
        }

        public MeasurementResultModel build() {
            return new MeasurementResultModel(this.name, this.secondaryResultModelList);
        }
    }
}
