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
import tkaxv7s.xposed.sesame.data.modelFieldExt.common.SelectModelFieldFunc;
import tkaxv7s.xposed.sesame.entity.IdAndName;
import tkaxv7s.xposed.sesame.entity.KVNode;
import tkaxv7s.xposed.sesame.ui.ListDialog;

import java.util.List;
import java.util.Objects;

public class SelectAndCountOneModelField extends ModelField<KVNode<String, Integer>> implements SelectModelFieldFunc {

    private SelectListFunc selectListFunc;

    private List<? extends IdAndName> expandValue;

    public SelectAndCountOneModelField(String code, String name, KVNode<String, Integer> value, List<? extends IdAndName> expandValue) {
        super(code, name, value);
        this.expandValue = expandValue;
    }

    public SelectAndCountOneModelField(String code, String name, KVNode<String, Integer> value, SelectListFunc selectListFunc) {
        super(code, name, value);
        this.selectListFunc = selectListFunc;
    }

    @Override
    public String getType() {
        return "SELECT_AND_COUNT_ONE";
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
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> ListDialog.show(v.getContext(), ((Button) v).getText(), this, ListDialog.ListType.RADIO));
        return btn;
    }

    @Override
    public void clear() {
        value = defaultValue;
    }

    @Override
    public Integer get(String id) {
        KVNode<String, Integer> kvNode = getValue();
        if (kvNode != null && Objects.equals(kvNode.getKey(), id)) {
            return kvNode.getValue();
        }
        return 0;
    }

    @Override
    public void add(String id, Integer count) {
        value = new KVNode<>(id, count);
    }

    @Override
    public void remove(String id) {
        KVNode<String, Integer> kvNode = getValue();
        if (kvNode != null && Objects.equals(kvNode.getKey(), id)) {
            value = defaultValue;
        }
    }

    @Override
    public Boolean contains(String id) {
        KVNode<String, Integer> kvNode = getValue();
        return kvNode != null && Objects.equals(kvNode.getKey(), id);
    }

    public interface SelectListFunc {
        List<? extends IdAndName> getList();
    }
}