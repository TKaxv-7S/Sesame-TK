
package tkaxv7s.xposed.sesame.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

public class TypeUtil {
    public TypeUtil() {
    }

    public static Class<?> getClass(Type type) {
        if (null != type) {
            if (type instanceof Class) {
                return (Class)type;
            }

            if (type instanceof ParameterizedType) {
                return (Class)((ParameterizedType)type).getRawType();
            }

            Type[] upperBounds;
            if (type instanceof TypeVariable) {
                upperBounds = ((TypeVariable)type).getBounds();
                if (upperBounds.length == 1) {
                    return getClass(upperBounds[0]);
                }
            } else if (type instanceof WildcardType) {
                upperBounds = ((WildcardType)type).getUpperBounds();
                if (upperBounds.length == 1) {
                    return getClass(upperBounds[0]);
                }
            }
        }

        return null;
    }

    public static Type getType(Field field) {
        return null == field ? null : field.getGenericType();
    }

    public static Class<?> getClass(Field field) {
        return null == field ? null : field.getType();
    }

    public static Type getFirstParamType(Method method) {
        return getParamType(method, 0);
    }

    public static Class<?> getFirstParamClass(Method method) {
        return getParamClass(method, 0);
    }

    public static Type getParamType(Method method, int index) {
        Type[] types = getParamTypes(method);
        return null != types && types.length > index ? types[index] : null;
    }

    public static Class<?> getParamClass(Method method, int index) {
        Class<?>[] classes = getParamClasses(method);
        return null != classes && classes.length > index ? classes[index] : null;
    }

    public static Type[] getParamTypes(Method method) {
        return null == method ? null : method.getGenericParameterTypes();
    }

    public static Class<?>[] getParamClasses(Method method) {
        return null == method ? null : method.getParameterTypes();
    }

    public static Type getReturnType(Method method) {
        return null == method ? null : method.getGenericReturnType();
    }

    public static Class<?> getReturnClass(Method method) {
        return null == method ? null : method.getReturnType();
    }

    public static Type getTypeArgument(Type type) {
        return getTypeArgument(type, 0);
    }

    public static Type getTypeArgument(Type type, int index) {
        Type[] typeArguments = getTypeArguments(type);
        return null != typeArguments && typeArguments.length > index ? typeArguments[index] : null;
    }

    public static Type[] getTypeArguments(Type type) {
        if (null == type) {
            return null;
        } else {
            ParameterizedType parameterizedType = toParameterizedType(type);
            return null == parameterizedType ? null : parameterizedType.getActualTypeArguments();
        }
    }

    public static ParameterizedType toParameterizedType(Type type) {
        return toParameterizedType(type, 0);
    }

    public static ParameterizedType toParameterizedType(Type type, int interfaceIndex) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType)type;
        } else {
            if (type instanceof Class) {
                ParameterizedType[] generics = getGenerics((Class)type);
                if (generics.length > interfaceIndex) {
                    return generics[interfaceIndex];
                }
            }

            return null;
        }
    }

    public static ParameterizedType[] getGenerics(Class<?> clazz) {
        List<ParameterizedType> result = new ArrayList();
        Type genericSuper = clazz.getGenericSuperclass();
        if (null != genericSuper && !Object.class.equals(genericSuper)) {
            ParameterizedType parameterizedType = toParameterizedType(genericSuper);
            if (null != parameterizedType) {
                result.add(parameterizedType);
            }
        }

        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            ParameterizedType parameterizedType = toParameterizedType(genericInterface);
            if (null != parameterizedType) {
                result.add(parameterizedType);
            }
        }

        return (ParameterizedType[])result.toArray(new ParameterizedType[0]);
    }

    public static boolean isUnknown(Type type) {
        return null == type || type instanceof TypeVariable;
    }

    public static boolean hasTypeVariable(Type... types) {
        Type[] var1 = types;
        int var2 = types.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Type type = var1[var3];
            if (type instanceof TypeVariable) {
                return true;
            }
        }

        return false;
    }

}
