package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.ui.StringDialog;

public class TextModelField extends ModelField {

    public TextModelField(String code, String name, String value) {
        super(code, name, value);
    }

    @Override
    public String getType() {
        return "TEXT";
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

    @JsonIgnore
    public View getView(Context context) {
        Button btn = new Button(context);
        btn.setText(getName());
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setTextColor(Color.parseColor("#216EEE"));
        btn.setBackground(context.getResources().getDrawable(R.drawable.button));
        btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        btn.setMinHeight(150);
        btn.setMaxHeight(180);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> StringDialog.showReadDialog(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

}
