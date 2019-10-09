package org.valkyrienskies.mod.common.command.config;

import java.lang.reflect.Field;
import javax.annotation.Nullable;

class ConfigCommandUtils {

    /**
     * Sets a field from a string, assuming the field is static. It only supports fields where
     * {@link ConfigCommandUtils#isSupportedType(Class)} for {@link Field#getType()}
     *
     * @param string The string to set the field's value to
     * @param field  The field to set
     */
    public static void setFieldFromString(String string, Field field) {
        setFieldFromString(string, field, null);
    }

    /**
     * Sets a field from a string. It only supports fields where {@link
     * ConfigCommandUtils#isSupportedType(Class)} for {@link Field#getType()}
     *
     * @param string The string to set the field's value to
     * @param field  The field to set
     * @param object The object upon which to set the field
     */
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

    /**
     * Whether or not this type is supported by {@link ConfigCommandUtils#setFieldFromString(String,
     * Field, Object)}
     *
     * @param type The type of the field
     */
    public static boolean isSupportedType(Class<?> type) {
        return type == int.class || type == double.class || type == float.class
            || type == boolean.class || type == byte.class || type == long.class
            || type == short.class || type == char.class || type == String.class;
    }

    /**
     * Basically a shortcut for <code>field.get(null).toString()</code>
     */
    public static String getStringFromField(Field field) {
        return getStringFromField(field, null);
    }


    /**
     * Basically a shortcut for <code>field.get(object).toString()</code>
     */
    public static String getStringFromField(Field field, @Nullable Object object) {
        try {
            return field.get(object).toString();
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

}
