package com.mzh.jpaconvertergenerator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates a method is a toValue.
 *
 * @author ChenLingshu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface EnumValue {
}
