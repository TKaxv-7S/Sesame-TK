package tkaxv7s.xposed.sesame.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tkaxv7s.xposed.sesame.util.UserIdMap;

public class AlipayUser extends IdAndName {
    private static List<AlipayUser> list;

    public AlipayUser(String i, String n) {
        id = i;
        name = n;
    }

    public static List<AlipayUser> getList() {
        if (list == null) {
            list = new ArrayList<>();
            Map<String, UserEntity> userIdMap = UserIdMap.getUserMap();
            for (Map.Entry<String, UserEntity> entry : userIdMap.entrySet()) {
                list.add(new AlipayUser(entry.getKey(), entry.getValue().getShowName()));
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
