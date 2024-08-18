package org.terraform.utils.injection;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InjectableObject {
    final static Map<Class<?>, Object> globals = new HashMap<>();
    Map<Class<?>, Object> registry = new HashMap<>();

    protected void postInit() {
        // Override if you need to do something special.
    }

    public @NotNull <T extends InjectableObject> T create(final Class<? extends InjectableObject> clazz, final Object... args) {
        T instance = createInstance(clazz, args);
        instance.registry = this.registry; // TODO: verify -- See that these pointers point to the whole chain!
        register(instance);
        injectDependencies(instance, registry, globals);
        instance.postInit();
        return instance;
    }

    public static @NotNull <T extends InjectableObject> T createGlobal(final Class<? extends InjectableObject> clazz, final Object... args) {
        T instance = createInstance(clazz, args);
        registerGlobal(instance);
        injectDependencies(instance, globals, null);
        instance.postInit();
        return instance;
    }

    public @NotNull <T> T register(final @NotNull T instance) {
        final Class<?> type = instance.getClass();
        Preconditions.checkState(!registry.containsKey(type), "MapProxy already contains key: " + type.getName());
        registry.put(type, instance);
        return instance;
    }

    public static @NotNull <T> T registerGlobal(final @NotNull T instance) {
        final Class<?> type = instance.getClass();
        Preconditions.checkState(!globals.containsKey(type), "MapProxy already contains key: " + type.getName());
        globals.put(type, instance);
        return instance;
    }

    private static void injectDependencies(final @NotNull Object target, final Map<Class<?>, Object> primaryMap, final Map<Class<?>, Object> secondaryMap) {
        Class<?> currentClass = target.getClass();
        List<Field> injectFields = new ArrayList<>();

        // Collect all fields annotated with @Inject from the class hierarchy
        while(currentClass != null) {
            for(Field field : currentClass.getDeclaredFields()) {
                if(field.isAnnotationPresent(Inject.class)) {
                    injectFields.add(field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        // Inject dependencies into collected fields
        for(Field field : injectFields) {
            Class<?> type = field.getType();

            Object dependency = primaryMap.get(type);
            if(dependency == null) {
                dependency = secondaryMap.get(type);
            }
            if(dependency == null) {
                throw new RuntimeException("Warning: Dependency not found for field: " + field.getName() + " in class: " + field.getDeclaringClass().getName());
            }

            field.setAccessible(true);
            try {
                field.set(target, dependency);
            } catch(IllegalAccessException e) {
                throw new RuntimeException("Failed to inject dependency into field: " + field.getName(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends InjectableObject> @NotNull T createInstance(final Class<? extends InjectableObject> clazz, final Object... args) {
        try {
            if(args.length == 0) {
                return (T) clazz.getDeclaredConstructor().newInstance();
            }

            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            for(Constructor<?> constructor : constructors) {
                if(isMatchingConstructor(constructor, args)) {
                    return (T) constructor.newInstance(args);
                }
            }
            throw new IllegalArgumentException("No suitable constructor found for " + clazz.getName() +
                    " with arguments: " + Arrays.toString(args));
        } catch(Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    private static boolean isMatchingConstructor(final @NotNull Constructor<?> constructor, final Object @NotNull [] args) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        if(paramTypes.length != args.length) {
            return false;
        }
        for(int i = 0; i < paramTypes.length; i++) {
            if(args[i] != null && !isAssignable(paramTypes[i], args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignable(final @NotNull Class<?> targetType, final Class<?> sourceType) {
        // Handle primitive types
        if(targetType.isPrimitive()) {
            return isPrimitiveAssignable(targetType, sourceType);
        }
        return targetType.isAssignableFrom(sourceType);
    }

    private static boolean isPrimitiveAssignable(final Class<?> targetType, final Class<?> sourceType) {
        if(targetType == boolean.class) return sourceType == Boolean.class;
        if(targetType == char.class) return sourceType == Character.class;
        if(targetType == byte.class) return sourceType == Byte.class;
        if(targetType == short.class) return sourceType == Short.class;
        if(targetType == int.class) return sourceType == Integer.class;
        if(targetType == long.class) return sourceType == Long.class;
        if(targetType == float.class) return sourceType == Float.class;
        if(targetType == double.class) return sourceType == Double.class;
        return false;
    }
}
