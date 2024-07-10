package tkaxv7s.xposed.sesame.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import tkaxv7s.xposed.sesame.util.HanziToPinyin;

import java.util.ArrayList;
import java.util.List;

public abstract class IdAndName implements Comparable<IdAndName> {

    @Getter
    public String name;

    @Getter
    public String id;

    private ArrayList<String> pinyin;

    @JsonIgnore
    public ArrayList<String> getPinyin() {
        if (pinyin != null) {
            return pinyin;
        }
        ArrayList<HanziToPinyin.Token> list = HanziToPinyin.getInstance().get(name);
        pinyin = new ArrayList<>(list.size());
        for (HanziToPinyin.Token token : list) {
            pinyin.add(token.target);
        }
        return pinyin;
    }

    @Override
    public int compareTo(IdAndName o) {
        List<String> list1 = this.getPinyin();
        List<String> list2 = o.getPinyin();
        int i = 0;
        while (i < list1.size() && i < list2.size()) {
            if (list1.get(i).compareTo(list2.get(i)) != 0) {
                return list1.get(i).compareTo(list2.get(i));
            }
            i++;
        }
        return list1.size() - list2.size();
    }
}
