package pansong291.xposed.quickenergy.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.fasterxml.jackson.annotation.JsonIgnore;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.ui.StringDialog;

public class IntegerModelField extends ModelField {

    protected Integer minLimit;

    protected Integer maxLimit;

    public IntegerModelField() {
    }

    public IntegerModelField(Object value) {
        super(value);
    }

    public IntegerModelField(String code, String name, Integer value) {
        super(code, name, value);
    }

    public IntegerModelField(String code, String name, Integer value, Integer minLimit, Integer maxLimit) {
        super(code, name, value);
        this.minLimit = minLimit;
        this.maxLimit = maxLimit;
    }

    @Override
    public void setValue(Object value) {
        Integer newValue;
        if (value == null) {
            newValue = (Integer) defaultValue;
        } else {
            newValue = Integer.parseInt(value.toString());
        }
        if (minLimit != null) {
            newValue = Math.max(minLimit, newValue);
        }
        if (maxLimit != null) {
            newValue = Math.min(maxLimit, newValue);
        }
        this.value = newValue;
    }

    @Override
    public Integer getValue() {
        return (Integer) value;
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
        btn.setOnClickListener(v -> StringDialog.showEditDialog(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

    public static class Multiply1000IntegerModelField extends IntegerModelField {

        public Multiply1000IntegerModelField() {
        }

        public Multiply1000IntegerModelField(String code, String name, Integer value) {
            super(code, name, value);
        }

        @Override
        public void setConfigValue(String value) {
            if (value == null) {
                setValue(null);
                return;
            }
            setValue(Integer.parseInt(value) * 1_000);
        }

        @Override
        public String getConfigValue() {
            return String.valueOf(getValue() / 1_000);
        }
    }

    public static class Limit0To100000IntegerModelField extends IntegerModelField {

        public Limit0To100000IntegerModelField() {
        }

        public Limit0To100000IntegerModelField(String code, String name, Integer value) {
            super(code, name, value);
        }

        @Override
        public void setConfigValue(String value) {
            if (value == null) {
                setValue(null);
                return;
            }
            int setNewValue = Integer.parseInt(value);
            if (setNewValue < 0) {
                setValue(0);
            } else if (setNewValue > 100000) {
                setValue(100000);
            }
        }

    }

}
