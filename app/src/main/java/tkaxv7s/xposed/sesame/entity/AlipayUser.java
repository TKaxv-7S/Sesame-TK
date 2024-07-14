package tkaxv7s.xposed.sesame.entity;

import tkaxv7s.xposed.sesame.util.Log;
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
        return getList(user -> true);
    }

    public static List<AlipayUser> getList(Filter filterFunc) {
        List<AlipayUser> list = new ArrayList<>();
        Map<String, UserEntity> userIdMap = UserIdMap.getUserMap();
        for (Map.Entry<String, UserEntity> entry : userIdMap.entrySet()) {
            UserEntity userEntity = entry.getValue();
            try {
                if (filterFunc.apply(userEntity)) {
                    list.add(new AlipayUser(entry.getKey(), userEntity.getFullName()));
                }
            } catch (Throwable t) {
                Log.printStackTrace(t);
            }
        }
        return list;
    }

    public interface Filter {

        Boolean apply(UserEntity user);

    }

}
