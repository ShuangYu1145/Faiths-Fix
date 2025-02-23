package dev.faiths.event;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EventTarget {
    public byte value() default 2;
}

