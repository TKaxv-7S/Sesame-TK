package tkaxv7s.xposed.sesame.data.modelFieldExt;


import tkaxv7s.xposed.sesame.data.ModelField;

public class TextModelField extends ModelField {

    public TextModelField(String code, String name, String value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = String.valueOf(value);
    }

    @Override
    public String getValue() {
        return (String) value;
    }

}
