package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.fasterxml.jackson.core.type.TypeReference;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.common.SelectModelFieldFunc;
import tkaxv7s.xposed.sesame.entity.IdAndName;
import tkaxv7s.xposed.sesame.ui.ListDialog;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据结构说明
 * Set<String> 表示已选择的数据
 * List<? extends IdAndName> 需要选择的数据
 */
public class SelectModelField extends ModelField<Set<String>> implements SelectModelFieldFunc {

    private static final TypeReference<LinkedHashSet<String>> typeReference = new TypeReference<LinkedHashSet<String>>() {
    };

    private SelectListFunc selectListFunc;

    private List<? extends IdAndName> expandValue;

    public SelectModelField(String code, String name, Set<String> value, List<? extends IdAndName> expandValue) {
        super(code, name, value);
        this.expandValue = expandValue;
    }

    public SelectModelField(String code, String name, Set<String> value, SelectListFunc selectListFunc) {
        super(code, name, value);
        this.selectListFunc = selectListFunc;
    }

    @Override
    public String getType() {
        return "SELECT";
    }

    public List<? extends IdAndName> getExpandValue() {
        return selectListFunc == null ? expandValue : selectListFunc.getList();
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
        btn.setOnClickListener(v -> ListDialog.show(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

    @Override
    public void clear() {
        getValue().clear();
    }

    @Override
    public Integer get(String id) {
        return 0;
    }

    @Override
    public void add(String id, Integer count) {
        getValue().add(id);
    }

    @Override
    public void remove(String id) {
        getValue().remove(id);
    }

    @Override
    public Boolean contains(String id) {
        return getValue().contains(id);
    }

    public interface SelectListFunc {
        List<? extends IdAndName> getList();
    }
}
