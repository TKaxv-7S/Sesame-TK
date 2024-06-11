package pansong291.xposed.quickenergy.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

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

    public static String toJsonString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
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

}
