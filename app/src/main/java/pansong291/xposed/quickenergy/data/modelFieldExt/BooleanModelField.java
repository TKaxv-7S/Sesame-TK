package pansong291.xposed.quickenergy.data.modelFieldExt;


import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.util.JsonUtil;

public class BooleanModelField extends ModelField {

    public BooleanModelField() {
    }

    public BooleanModelField(String code, String name, Boolean value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        this.value = JsonUtil.parseObject(value, Boolean.class);
    }

    @Override
    public Boolean getValue() {
        return (Boolean) value;
    }

}
