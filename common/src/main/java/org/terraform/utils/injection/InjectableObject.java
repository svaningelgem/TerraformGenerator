package org.terraform.utils.injection;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface InjectableObject {
    Map<Class<?>, Object> registry = new HashMap<>();

    default void postInit() {
        // Override if you need to do something special.
    }

    default <T extends InjectableObject> T create(Class<? extends InjectableObject> clazz, Object... args) {
        T instance = createInstance(clazz, args);
        register(instance);
        inject(instance);
        instance.postInit();
        return instance;
    }

    default <T> T register(T instance) {
        registry.put(instance.getClass(), instance);
        return instance;
    }

    private void inject(@NotNull Object target) {
        Class<?> currentClass = target.getClass();
        List<Field> injectFields = new ArrayList<>();

        // Collect all fields annotated with @Inject from the class hierarchy
        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    injectFields.add(field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        // Inject dependencies into collected fields
        for (Field field : injectFields) {
            Object dependency = registry.get(field.getType());
            if (dependency != null) {
                field.setAccessible(true);
                try {
                    field.set(target, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency into field: " + field.getName(), e);
                }
            } else {
                throw new RuntimeException("Warning: Dependency not found for field: " + field.getName() + " in class: " + field.getDeclaringClass().getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends InjectableObject> @NotNull T createInstance(Class<? extends InjectableObject> clazz, Object... args) {
        try {
            if (args.length == 0) {
                return (T) clazz.getDeclaredConstructor().newInstance();
            } else {
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                for (Constructor<?> constructor : constructors) {
                    if (isMatchingConstructor(constructor, args)) {
                        return (T) constructor.newInstance(args);
                    }
                }
                throw new IllegalArgumentException("No suitable constructor found for " + clazz.getName() +
                        " with arguments: " + Arrays.toString(args));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    private boolean isMatchingConstructor(@NotNull Constructor<?> constructor, Object @NotNull [] args) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        if (paramTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] != null && !isAssignable(paramTypes[i], args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private boolean isAssignable(@NotNull Class<?> targetType, Class<?> sourceType) {
        // Handle primitive types
        if (targetType.isPrimitive()) {
            return isPrimitiveAssignable(targetType, sourceType);
        }
        return targetType.isAssignableFrom(sourceType);
    }

    private boolean isPrimitiveAssignable(Class<?> targetType, Class<?> sourceType) {
        if (targetType == boolean.class) return sourceType == Boolean.class;
        if (targetType == char.class) return sourceType == Character.class;
        if (targetType == byte.class) return sourceType == Byte.class;
        if (targetType == short.class) return sourceType == Short.class;
        if (targetType == int.class) return sourceType == Integer.class;
        if (targetType == long.class) return sourceType == Long.class;
        if (targetType == float.class) return sourceType == Float.class;
        if (targetType == double.class) return sourceType == Double.class;
        return false;
    }
}
