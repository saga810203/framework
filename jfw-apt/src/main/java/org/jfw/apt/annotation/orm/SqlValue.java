package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.orm.core.enums.DE;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface SqlValue {
	DE de();
	String paramName() default "";
	String attributeName() default "";
	// read value from method parameter
	String paramExpression() default "";
	boolean nullable() default false;
	String sqlExpression() default "";
}
