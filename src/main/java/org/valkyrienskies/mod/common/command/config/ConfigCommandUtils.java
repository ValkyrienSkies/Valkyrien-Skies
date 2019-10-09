package org.valkyrienskies.mod.common.command.config;

import java.lang.reflect.Field;
import javax.annotation.Nullable;

public class ConfigCommandUtils {

    public static void setFieldFromString(String string, Field field) {
        setFieldFromString(string, field, null);
    }

    public static void setFieldFromString(String string, Field field, @Nullable Object object) {
        if (!isSupportedType(field.getType())) {
            throw new IllegalArgumentException("Unsupported field type");
        }
        try {
            if (field.getType() == int.class) {
                field.setInt(object, Integer.parseInt(string));
            } else if (field.getType() == double.class) {
                field.setDouble(object, Double.parseDouble(string));
            } else if (field.getType() == float.class) {
                field.setFloat(object, Float.parseFloat(string));
            } else if (field.getType() == boolean.class) {
                field.setBoolean(object, Boolean.parseBoolean(string));
            } else if (field.getType() == byte.class) {
                field.setByte(object, Byte.parseByte(string));
            } else if (field.getType() == long.class) {
                field.setLong(object, Long.parseLong(string));
            } else if (field.getType() == short.class) {
                field.setShort(object, Short.parseShort(string));
            } else if (field.getType() == char.class) {
                field.setChar(object, string.charAt(0));
            } else if (field.getType() == String.class) {
                field.set(object, string);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isSupportedType(Class<?> type) {
        return type == int.class || type == double.class || type == float.class
            || type == boolean.class || type == byte.class || type == long.class
            || type == short.class || type == char.class || type == String.class;
    }

    public static String getStringFromField(Field field) {
        return getStringFromField(field, null);
    }

    public static String getStringFromField(Field field, @Nullable Object object) {
        try {
            return field.get(object).toString();
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

}
