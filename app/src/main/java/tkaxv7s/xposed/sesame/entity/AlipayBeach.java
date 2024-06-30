package tkaxv7s.xposed.sesame.entity;

import tkaxv7s.xposed.sesame.util.BeachIdMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlipayBeach extends IdAndName {
    private static List<AlipayBeach> list;

    public AlipayBeach(String i, String n) {
        id = i;
        name = n;
    }

    public static List<AlipayBeach> getList() {
        if (list == null) {
            list = new ArrayList<>();
            for (Map.Entry<String, String> entry : BeachIdMap.getMap().entrySet()) {
                list.add(new AlipayBeach(entry.getKey(), entry.getValue()));
            }
        }
        return list;
    }

    public static void remove(String id) {
        getList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(id)) {
                list.remove(i);
                break;
            }
        }
    }

}
