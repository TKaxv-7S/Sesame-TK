package tkaxv7s.xposed.sesame.data.modelFieldExt.common;

import java.util.LinkedHashMap;
import java.util.Map;

public interface SelectModelFieldFunc {

    void clear();

    Integer get(String id);

    void add(String id, Integer count);

    void remove(String id);

    Boolean contains(String id);


    static SelectModelFieldFunc newMapInstance() {
        return new SelectModelFieldFunc() {

            private final Map<String, Integer> map = new LinkedHashMap<>();

            @Override
            public void clear() {
                map.clear();
            }

            @Override
            public Integer get(String id) {
                return map.get(id);
            }

            @Override
            public void add(String id, Integer count) {
                map.put(id, count);
            }

            @Override
            public void remove(String id) {
                map.remove(id);
            }

            @Override
            public Boolean contains(String id) {
                return map.containsKey(id);
            }
        };
    }
}