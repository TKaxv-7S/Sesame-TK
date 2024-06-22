package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.ui.StringDialog;
import tkaxv7s.xposed.sesame.util.Log;

public class IntegerModelField extends ModelField {

    private Integer minLimit;

    private Integer maxLimit;

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
        btn.setMaxHeight(180);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> StringDialog.showEditDialog(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

    public static class MultiplyIntegerModelField extends IntegerModelField {

        private final Integer multiple;

        public MultiplyIntegerModelField(String code, String name, Integer value, Integer minLimit, Integer maxLimit, Integer multiple) {
            super(code, name, value, minLimit, maxLimit);
            this.multiple = multiple;
        }

        @Override
        public void setConfigValue(String value) {
            if (value == null) {
                setValue(null);
                return;
            }
            try {
                setValue(Integer.parseInt(value) * multiple);
            } catch (Exception e) {
                Log.printStackTrace(e);
                setValue(null);
            }
        }

        @Override
        public String getConfigValue() {
            return String.valueOf(getValue() / multiple);
        }
    }

}
