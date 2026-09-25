package dev.lokspel.deathswap.util;

import org.bukkit.GameRule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static Class<?> classOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    public static Method methodOrNull(Class<?> clazz, String name, Class<?>... params) {
        try {
            // getMethod() searches for public methods, including inherited ones
            Method method = clazz.getMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> GameRule<T> gameRuleOrNull(String name) {
        Object modernRule = staticFieldOrNull(
                "org.bukkit.GameRules",
                name
        );

        if (modernRule instanceof GameRule<?> gameRule) {
            return (GameRule<T>) gameRule;
        }

        Object legacyRule = staticFieldOrNull(
                GameRule.class,
                name
        );

        if (legacyRule instanceof GameRule<?> gameRule) {
            return (GameRule<T>) gameRule;
        }

        return null;
    }

    public static Object staticFieldOrNull(
            String className,
            String fieldName
    ) {
        Class<?> type = classOrNull(className);

        if (type == null) {
            return null;
        }

        return staticFieldOrNull(type, fieldName);
    }

    public static Object staticFieldOrNull(
            Class<?> type,
            String fieldName
    ) {
        try {
            Field field = type.getField(fieldName);
            return field.get(null);
        } catch (NoSuchFieldException
                 | IllegalAccessException
                 | SecurityException exception) {
            return null;
        }
    }
}