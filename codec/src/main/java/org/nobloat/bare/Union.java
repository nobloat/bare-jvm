package org.nobloat.bare;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

public class Union {

    Map<Long, Class<?>> types;
    Object value;
    long type;

    public Union(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Union(Class<?> ...allowedTypes) {
        this.types = new HashMap<>();
        for (var c : allowedTypes) {
            var unionId = c.getAnnotation(Id.class);
            if (unionId == null) {
                throw new UnsupportedOperationException("Missing annotation @Union.Id on " + c.getName());
            }
            this.types.put(unionId.value(), c);
        }
    }

    public void set(long id, Object object) {
        if (types.containsKey(id)) {
            this.value = object;
            this.type = id;
        } else {
            throw new UnsupportedOperationException("Could not map union type: " + id);
        }
    }

    Class<?> type(long id) {
        if (types.containsKey(id)) {
            return types.get(id);
        }
        throw new UnsupportedOperationException("Unexpected union type: " + id);
    }

    public int type() {
        return (int) type;
    }

    public <T> T get(Class<T> type) {
        return (T)value;
    }

    @Override
    public String toString() {
        return "Union{" +
                "value=" + value +
                '}';
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Id {
        long value() default 0;
    }

}
