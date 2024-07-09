package tkaxv7s.xposed.sesame.util;

import android.annotation.SuppressLint;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class JsonUtil {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    public static final JsonFactory JSON_FACTORY = new JsonFactory();

    static {
        //反序列化的时候如果多了其他属性,不抛出异常
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //如果是空对象的时候,不抛异常
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //属性为null不转换
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.setTimeZone(TimeZone.getDefault());
        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static ObjectMapper copyMapper() {
        return MAPPER.copy();
    }

    public static String toNoFormatJsonString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJsonString(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonParser getJsonParser(String body) {
        try {
            return JSON_FACTORY.createParser(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String body, Type type) {
        try {
            return MAPPER.readValue(body, TYPE_FACTORY.constructType(type));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String body, JavaType javaType) {
        try {
            return MAPPER.readValue(body, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String body, TypeReference<T> valueTypeRef) {
        try {
            return MAPPER.readValue(body, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String body, Class<T> clazz) {
        try {
            return MAPPER.readValue(body, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(JsonParser jsonParser, Type type) {
        try {
            return MAPPER.readValue(jsonParser, TYPE_FACTORY.constructType(type));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(JsonParser jsonParser, JavaType javaType) {
        try {
            return MAPPER.readValue(jsonParser, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(JsonParser jsonParser, TypeReference<T> valueTypeRef) {
        try {
            return MAPPER.readValue(jsonParser, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(JsonParser jsonParser, Class<T> clazz) {
        try {
            return MAPPER.readValue(jsonParser, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(Object bean, Type type) {
        try {
            return MAPPER.convertValue(bean, TYPE_FACTORY.constructType(type));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(Object bean, JavaType javaType) {
        try {
            return MAPPER.convertValue(bean, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(Object bean, TypeReference<T> valueTypeRef) {
        try {
            return MAPPER.convertValue(bean, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(Object bean, Class<T> clazz) {
        try {
            return MAPPER.convertValue(bean, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String parseString(String body, String field) {
        JsonNode node = null;
        try {
            node = MAPPER.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null) {
                return leaf.asText();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Integer parseInteger(String body, String field) {
        JsonNode node = null;
        try {
            node = MAPPER.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null) {
                return leaf.asInt();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Integer> parseIntegerList(String body, String field) {
        JsonNode node = null;
        try {
            node = MAPPER.readTree(body);
            JsonNode leaf = node.get(field);

            if (leaf != null) {
                return MAPPER.convertValue(leaf, new TypeReference<List<Integer>>() {
                });
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Boolean parseBoolean(String body, String field) {
        JsonNode node = null;
        try {
            node = MAPPER.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null) {
                return leaf.asBoolean();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Short parseShort(String body, String field) {
        JsonNode node = null;
        try {
            node = MAPPER.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null) {
                Integer value = leaf.asInt();
                return value.shortValue();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Byte parseByte(String body, String field) {
        JsonNode node = null;
        try {
            node = MAPPER.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null) {
                Integer value = leaf.asInt();
                return value.byteValue();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String body, String field, Class<T> clazz) {
        JsonNode node = null;
        try {
            node = MAPPER.readTree(body);
            node = node.get(field);
            return MAPPER.treeToValue(node, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseList(String body, Class<T> clazz) {
        try {
            return MAPPER.readValue(body, TYPE_FACTORY.constructCollectionType(ArrayList.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object toNode(String json) {
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 根据给定的点分隔符路径从JSONObject中获取值。
     *
     * @param jsonObject JSONObject对象
     * @param path       点分隔符或包含嵌套属性的形式的路径，例如 "taskExtProps.TASK_MORPHO_DETAIL.[0].title"
     * @return 找到值的话返回其String表现形式；如果任何层级键不存在或不是预期类型，则返回 null。
     */
    public static String getValueByPath(JSONObject jsonObject, String path) {
        Object object = getValueByPathObject(jsonObject, path);
        return object == null ? "" : String.valueOf(object);
    }

    public static Object getValueByPathObject(JSONObject jsonObject, String path) {
        // 使用正斜杠/作为Token分隔符号来直接跳过数组下标的解析逻辑部分并直接取嵌套属性
        // 使用正则表达式分割，但保留[]内的内容
        String[] parts = path.split("\\.");
        try {
            Object current = jsonObject;
            for (String part : parts) {
                if (current instanceof JSONObject) {
                    //对象取属性
                    current = ((JSONObject) current).get(part);
                } else if (current instanceof JSONArray) {
                    //数组取索引
                    JSONArray array = (JSONArray) current;
                    String p = part.replaceAll("\\D", "");
                    int index = Integer.parseInt(p);
                    current = array.get(index);
                } else if (part.contains("[")) {
                    //不是对象、数组，当成字符串重新解析，如果字符串是数组
                    JSONArray array = new JSONArray(current.toString());
                    String p = part.replaceAll("\\D", "");
                    int index = Integer.parseInt(p);
                    current = array.get(index);
                } else {
                    //不是对象、数组，当成字符串重新解析，再取属性
                    JSONObject object = new JSONObject(current.toString());
                    current = object.get(part);
                }
            }
            // 返回结果时检查是否确实找到了相应的值且非null，并转换成字符串形式返回
            return current;
        } catch (Exception e) {
            // JSONException、NumberFormatException等异常都被捕获，并默认行为是返回null.
            return null;
        }
    }

    public static List<String> jsonArrayToList(JSONArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (int i = 0, len = jsonArray.length(); i < len; i++) {
            try {
                list.add(jsonArray.getString(i));
            } catch (Exception e) {
                Log.printStackTrace(e);
                list.add("");
            }
        }
        return list;
    }

}
