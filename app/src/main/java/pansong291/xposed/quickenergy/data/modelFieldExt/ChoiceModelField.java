package pansong291.xposed.quickenergy.data.modelFieldExt;


import com.fasterxml.jackson.annotation.JsonIgnore;

import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.task.model.antFarm.AntFarm;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.JsonUtil;

public class ChoiceModelField extends ModelField {

    public ChoiceModelField() {
    }

    public ChoiceModelField(String code, String name, Integer value) {
        super(code, name, value);
    }

    @JsonIgnore
    public CharSequence[] getList() {
        return new CharSequence[]{};
    }

    @Override
    public void setValue(Object value) {
        this.value = JsonUtil.parseObject(value, Integer.class);
    }

    @Override
    public Integer getValue() {
        return (Integer) value;
    }

    public static class RecallAnimalChoiceModelField extends ChoiceModelField {

        public RecallAnimalChoiceModelField() {
        }

        public RecallAnimalChoiceModelField(String code, String name, Integer value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public CharSequence[] getList() {
            return Config.RecallAnimalType.nickNames;
        }
    }

    public static class SendChoiceModelField extends ChoiceModelField {

        public SendChoiceModelField() {
        }

        public SendChoiceModelField(String code, String name, Integer value) {
            super(code, name, value);
        }

        @Override
        @JsonIgnore
        public CharSequence[] getList() {
            return AntFarm.SendType.nickNames;
        }
    }

}
