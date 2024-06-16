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
import pansong291.xposed.quickenergy.task.model.antFarm.AntFarm;
import pansong291.xposed.quickenergy.ui.ChoiceDialog;
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
        btn.setOnClickListener(v -> ChoiceDialog.show(v.getContext(), ((Button) v).getText(), this));
        return btn;
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
