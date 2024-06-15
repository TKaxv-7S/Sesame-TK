package pansong291.xposed.quickenergy.data.modelFieldExt;


import pansong291.xposed.quickenergy.data.ModelField;

public class StringModelField extends ModelField {

    public StringModelField() {
    }

    public StringModelField(String code, String name, String value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        if (value != null) {
            value = String.valueOf(value);
        }
        this.value = value;
    }

    @Override
    public String getValue() {
        return (String) value;
    }

}
