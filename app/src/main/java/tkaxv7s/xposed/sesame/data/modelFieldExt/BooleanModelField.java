package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.util.JsonUtil;

public class BooleanModelField extends ModelField {

    public BooleanModelField() {
    }

    public BooleanModelField(String code, String name, Boolean value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = JsonUtil.parseObject(value, Boolean.class);
    }

    @Override
    public Boolean getValue() {
        return (Boolean) value;
    }

    @Override
    public View getView(Context context) {
        Switch sw = new Switch(context);
        sw.setText(getName());
        sw.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        sw.setMinHeight(150);
        sw.setMaxHeight(180);
        sw.setPaddingRelative(40, 0, 40, 0);
        sw.setChecked(getValue());
        sw.setOnClickListener(v -> setValue(((Switch) v).isChecked()));
        return sw;
    }

}
