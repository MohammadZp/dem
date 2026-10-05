package org.example.di;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class Injector {

    public <T> T getInstance(Class<T> clazz) {
        try {
            Class<? extends T> implementation = resolveImplementation(clazz);

            Constructor<?> constructor =
                    implementation.getDeclaredConstructors()[0];

            Class<?>[] parameterTypes = constructor.getParameterTypes();

            Object[] dependencies = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                dependencies[i] = getInstance(parameterTypes[i]);
            }

            @SuppressWarnings("unchecked")
            T instance = (T) constructor.newInstance(dependencies);

            return instance;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot create instance of " + clazz.getName(), e);
        }
    }

    private <T> Class<? extends T> resolveImplementation(Class<T> clazz)
            throws ClassNotFoundException {

        if (!clazz.isInterface()) {
            return clazz;
        }

        String packageName = clazz.getPackageName();

        for (Class<?> candidate : findClasses(packageName)) {

            if (clazz.isAssignableFrom(candidate)
                    && !candidate.isInterface()
                    && !java.lang.reflect.Modifier.isAbstract(
                    candidate.getModifiers())) {

                @SuppressWarnings("unchecked")
                Class<? extends T> result =
                        (Class<? extends T>) candidate;

                return result;
            }
        }

        throw new RuntimeException(
                "No implementation found for " + clazz.getName());
    }

    private List<Class<?>> findClasses(String packageName)
            throws ClassNotFoundException {

        List<Class<?>> classes = new ArrayList<>();

        String path = packageName.replace('.', '/');

        ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();

        var resource = classLoader.getResource(path);

        if (resource == null) {
            return classes;
        }

        File directory = new File(resource.getFile());

        File[] files = directory.listFiles();

        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.getName().endsWith(".class")) {

                String className =
                        packageName + "." +
                                file.getName().replace(".class", "");

                classes.add(Class.forName(className));
            }
        }

        return classes;
    }
}