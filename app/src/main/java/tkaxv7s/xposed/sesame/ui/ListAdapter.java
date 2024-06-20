package tkaxv7s.xposed.sesame.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.entity.IdAndName;
import tkaxv7s.xposed.sesame.util.Log;

public class ListAdapter extends BaseAdapter {
    private static ListAdapter adapter;

    private static ListDialog.ListType listType;

    public static ListAdapter get(Context c) {
        if (adapter == null)
            adapter = new ListAdapter(c);
        adapter.findIndex = -1;
        adapter.findWord = null;
        return adapter;
    }

    public static ListAdapter get(Context c, ListDialog.ListType listType) {
        if (adapter == null) {
            adapter = new ListAdapter(c);
            viewHolderList = new ArrayList<>();
        }
        ListAdapter.listType = listType;
        adapter.findIndex = -1;
        adapter.findWord = null;
        return adapter;
    }

    Context context;
    List<? extends IdAndName> list;
    Map<String, Integer> selectedMap;
    int findIndex = -1;
    CharSequence findWord = null;

    private ListAdapter(Context c) {
        context = c;
    }

    public void setBaseList(List<? extends IdAndName> l) {
        if (l != list)
            exitFind();
        list = l;
    }

    public void setSelectedList(Map<String, Integer> selectedMap) {
        this.selectedMap = selectedMap;
        try {
            Collections.sort(list, (o1, o2) -> {
                if (this.selectedMap.containsKey(o1.id) == this.selectedMap.containsKey(o2.id)) {
                    return o1.compareTo(o2);
                }
                return this.selectedMap.containsKey(o1.id) ? -1 : 1;
            });
        } catch (Throwable t) {
            Log.i("ListAdapter err");
            Log.printStackTrace(t);
        }
    }

    public int findLast(CharSequence cs) {
        if (list == null || list.isEmpty())
            return -1;
        if (!cs.equals(findWord)) {
            findIndex = -1;
            findWord = cs;
        }
        int i = findIndex;
        if (i < 0)
            i = list.size();
        for (; ; ) {
            i = (i + list.size() - 1) % list.size();
            IdAndName ai = list.get(i);
            if (ai.name.contains(cs)) {
                findIndex = i;
                break;
            }
            if (findIndex < 0 && i == 0)
                break;
        }
        notifyDataSetChanged();
        return findIndex;
    }

    public int findNext(CharSequence cs) {
        if (list == null || list.isEmpty())
            return -1;
        if (!cs.equals(findWord)) {
            findIndex = -1;
            findWord = cs;
        }
        for (int i = findIndex; ; ) {
            i = (i + 1) % list.size();
            IdAndName ai = list.get(i);
            if (ai.name.contains(cs)) {
                findIndex = i;
                break;
            }
            if (findIndex < 0 && i == list.size() - 1)
                break;
        }
        notifyDataSetChanged();
        return findIndex;
    }

    public void exitFind() {
        findIndex = -1;
    }

    public void selectAll() {
        selectedMap.clear();
        for (IdAndName ai : list) {
            selectedMap.put(ai.id, 0);
        }
        notifyDataSetChanged();
    }

    public void SelectInvert() {
        for (IdAndName ai : list) {
            if (!selectedMap.containsKey(ai.id)) {
                selectedMap.put(ai.id, 0);
            } else {
                selectedMap.remove(ai.id);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int p1) {
        return list.get(p1);
    }

    @Override
    public long getItemId(int p1) {
        return p1;
    }

    @Override
    public View getView(int p1, View p2, ViewGroup p3) {
        ViewHolder vh;
        if (p2 == null) {
            vh = new ViewHolder();
            p2 = View.inflate(context, R.layout.list_item, null);
            vh.tv = p2.findViewById(R.id.tv_idn);
            vh.cb = p2.findViewById(R.id.cb_list);
            if (listType == ListDialog.ListType.SHOW) {
                vh.cb.setVisibility(View.GONE);
            }
            p2.setTag(vh);
            viewHolderList.add(vh);
        } else {
            vh = (ViewHolder) p2.getTag();
        }

        IdAndName ai = list.get(p1);
        vh.tv.setText(ai.name);
        vh.tv.setTextColor(findIndex == p1 ? Color.RED : Color.BLACK);
        vh.cb.setChecked(selectedMap != null && selectedMap.containsKey(ai.id));
        return p2;
    }

    public static List<ViewHolder> viewHolderList;

    public static class ViewHolder {
        TextView tv;
        CheckBox cb;
    }

}
