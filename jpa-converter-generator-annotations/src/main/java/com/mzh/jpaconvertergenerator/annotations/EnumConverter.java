package com.mzh.jpaconvertergenerator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an enumeration class need generated a JPA Converter.
 *
 * @author ChenLingshu
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface EnumConverter {

    /**
     * @return boolean
     */
    boolean autoApply() default true;

}
