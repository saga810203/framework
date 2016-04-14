package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Where {
	String sentence() default "";
	boolean and() default true;
	boolean dynamic() default false;
	SqlValue[] values() default {};
}
