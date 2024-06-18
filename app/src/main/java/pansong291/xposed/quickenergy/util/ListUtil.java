package pansong291.xposed.quickenergy.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtil {
    private static final String TAG = ListUtil.class.getSimpleName();

    @SafeVarargs
    public static <T> List<T> newArrayList(T... objects) {
        List<T> list = new ArrayList<>();
        if (objects != null) {
            Collections.addAll(list, objects);
        }
        return list;
    }
}
