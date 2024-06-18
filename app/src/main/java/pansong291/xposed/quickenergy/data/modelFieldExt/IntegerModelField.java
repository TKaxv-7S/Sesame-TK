package pansong291.xposed.quickenergy.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.ui.EditDialog;

public class IntegerModelField extends ModelField {

    public IntegerModelField() {
    }

    public IntegerModelField(String code, String name, Integer value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = Integer.parseInt(value.toString());
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

    @Override
    public View getView(Context context) {
        Button btn = new Button(context);
        btn.setText(getName());
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setTextColor(Color.parseColor("#008175"));
        btn.setBackground(context.getResources().getDrawable(R.drawable.button));
        btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        btn.setMinHeight(150);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> EditDialog.showEditDialog(v.getContext(), ((Button) v).getText(), this));
        return btn;
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

    public static class Limit0To100000IntegerModelField extends IntegerModelField {

        public Limit0To100000IntegerModelField() {
        }

        public Limit0To100000IntegerModelField(String code, String name, Integer value) {
            super(code, name, value);
        }

        @Override
        public void setConfigValue(Object newValue) {
            setValue(newValue);
            Integer setNewValue = getValue();
            if (setNewValue !=null) {
                if (setNewValue < 0) {
                    value = 0;
                } else if (setNewValue > 100000) {
                    value = 100000;
                }
            }
        }

    }

}
