package ir.dotin.exam.di;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {
    private static final Map<Class<?>, Object> context = new HashMap<>();

    public void addToContext(Class<?> aClass, Object object) {
        context.putIfAbsent(aClass, object);
    }

    public Object getBean(Class<?> aClass) {
        return context.get(aClass);
    }
}
