package org.nobloat.bare;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Int {
    Type value() default Type.i32;

    enum Type {
        u8, i8, u16, i16, u32, i32, u64, i64
    }
}
