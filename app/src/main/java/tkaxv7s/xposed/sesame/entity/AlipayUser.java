package tkaxv7s.xposed.sesame.entity;

import tkaxv7s.xposed.sesame.util.UserIdMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlipayUser extends IdAndName {

    public AlipayUser(String i, String n) {
        id = i;
        name = n;
    }

    public static List<AlipayUser> getList() {
        List<AlipayUser> list = new ArrayList<>();
        Map<String, UserEntity> userIdMap = UserIdMap.getUserMap();
        for (Map.Entry<String, UserEntity> entry : userIdMap.entrySet()) {
            list.add(new AlipayUser(entry.getKey(), entry.getValue().getFullName()));
        }
        return list;
    }

}
