package tkaxv7s.xposed.sesame.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReserveIdMap {

    private static final Map<String, String> idMap = new ConcurrentHashMap<>();

    private static final Map<String, String> readOnlyIdMap = Collections.unmodifiableMap(idMap);

    public static Map<String, String> getMap() {
        return readOnlyIdMap;
    }

    public static String get(String key) {
        return idMap.get(key);
    }

    public synchronized static void add(String key, String value) {
        idMap.put(key, value);
    }

    public synchronized static void remove(String key) {
        idMap.remove(key);
    }

    public synchronized static void load() {
        idMap.clear();
        try {
            String body = FileUtil.readFromFile(FileUtil.getReserveIdMapFile());
            if (!body.isEmpty()) {
                Map<String, String> newMap = JsonUtil.parseObject(body, new TypeReference<Map<String, String>>() {
                });
                idMap.putAll(newMap);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public synchronized static boolean save() {
        return FileUtil.write2File(JsonUtil.toJsonString(idMap), FileUtil.getReserveIdMapFile());
    }

    public synchronized static void clear() {
        idMap.clear();
    }

}
