package pansong291.xposed.quickenergy.data.modelFieldExt;


import pansong291.xposed.quickenergy.data.ModelField;

public class IntegerModelField extends ModelField {

    public IntegerModelField() {
    }

    public IntegerModelField(String code, String name, Integer value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        if (value != null) {
            value = Integer.parseInt(value.toString());
        }
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return (Integer) value;
    }

    @Override
    public Integer getConfigValue() {
        return getValue();
    }

    @Override
    public void setConfigValue(Object matchValue) {
        setValue(matchValue);
    }

    public static class Multiply1000IntegerModelField extends IntegerModelField {

        public Multiply1000IntegerModelField() {
        }

        public Multiply1000IntegerModelField(String code, String name, Integer value) {
            super(code, name, value);
        }

        @Override
        public void setConfigValue(Object newValue) {
            setValue(newValue);
            value = getValue() * 1_000;
        }

        @Override
        public Integer getConfigValue() {
            return getValue() / 1_000;
        }
    }

}
