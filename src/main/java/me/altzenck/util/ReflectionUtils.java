package me.altzenck.util;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.*;
import java.util.Objects;

public class ReflectionUtils {

    public static void setAccessible(AccessibleObject... objects) {
        for (AccessibleObject object : objects) {
            object.setAccessible(true);
        }
    }

    public static <O> FieldReference<O> getDeclaredField(@Nullable Class<?> clazz, @Nullable final Object o, @NonNull final String fieldName) {
        Preconditions.checkArgument(clazz == null ^ o == null);
        if(clazz == null) clazz = o.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return new FieldReference<>(o, field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("o", e);
        }
    }

    public static <O> FieldReference<O> getDeclaredField(@NonNull final Object o, @NonNull final String fieldName) {
        return getDeclaredField(null, o, fieldName);
    }

    public static <O> FieldReference<O> getDeclaredField(@NonNull final Class<?> clazz, @NonNull final String fieldName) {
        return getDeclaredField(clazz, null, fieldName);
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <O> O callMethod(@Nullable Class<?> clazz, @Nullable final Object o, @NonNull String methodName, Object... args) {
        Preconditions.checkArgument(clazz == null ^ o == null);
        if(clazz == null) clazz = o.getClass();
        try {
            Method[] methods = clazz.getDeclaredMethods();
            for(Method m : methods) {
                if(!m.getName().equals(methodName) || Modifier.isStatic(m.getModifiers()) && o != null) continue;
                m.setAccessible(true);
                Parameter[] parameters = m.getParameters();
                if(parameters.length != args.length) continue;
                boolean success = true;
                for(int i = 0; i < parameters.length; i++) {
                    Class<?> clazzObj = args[i].getClass(),
                            clazzParam = parameters[i].getClass();
                    if(!clazzParam.isAssignableFrom(clazzObj)) {
                        success = false;
                        break;
                    }
                }
                if(success)
                    return (O) m.invoke(o, args);
            }
            throw new NoSuchMethodException(methodName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static @NonNull <O> O callMethod(@Nullable final Object o, @NonNull String methodName, Object... args) {
        return callMethod((Class<?>) o, null, methodName, args);
    }

    public static @NonNull <O> O callMethod(@Nullable Class<?> clazz, @NonNull String methodName, Object... args) {
        return callMethod(clazz, null, methodName, args);
    }

    @SuppressWarnings("unchecked")
    public static <O> O callMethod(@Nullable Class<?> clazz, @Nullable final Object o, @NonNull String methodName, @NonNull Class<?>[] clazzParameters, Object... args) {
        Preconditions.checkArgument(clazz == null ^ o == null);
        if(clazz == null) clazz = o.getClass();
        try {
            Method method = clazz.getDeclaredMethod(methodName, clazzParameters);
            method.setAccessible(true);
            return (O) method.invoke(o, args);
        } catch (NoSuchMethodException e1) {
            throw new IllegalArgumentException("methodName");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static @NonNull <O> O callMethod(@Nullable final Object o, @NonNull String methodName, Class<?>[] clazzParameters, Object... args) {
        return callMethod((Class<?>) o, null, methodName, clazzParameters, args);
    }

    public static @NonNull <O> O callMethod(@Nullable Class<?> clazz, @NonNull String methodName, Class<?>[] clazzParameters, Object... args) {
        return callMethod(clazz, null, methodName, clazzParameters, args);
    }

    private ReflectionUtils() {
        // Hide the public constructor
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FieldReference<O> {

        private final Object o;
        private final Field field;

        @SuppressWarnings("unchecked")
        public O get() {
            try {
                return (O) field.get(o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public <C> C cast(@NonNull Class<C> clazz) {
            return (C) get();
        }

        public void set(@Nullable O newValue) {
            try {
                field.set(o, newValue);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean equals(Object o) {
            if(o instanceof FieldReference<?>) {
                FieldReference<?> fr = (FieldReference<?>) o;
                return Objects.equals(get(), fr.get());
            }
            return get() == o;
        }

        public boolean isNull() {
            return get() == null;
        }

        public Field field() {
            return field;
        }

        public Object object() {
            return o;
        }
    }
}
