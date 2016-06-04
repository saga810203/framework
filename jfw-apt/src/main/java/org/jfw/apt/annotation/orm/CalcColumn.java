package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.orm.core.enums.DE;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface CalcColumn {
	DE value();	
	String calcExpression();
	String alias() default "";
	boolean nullable() default true;
	String comment() default "";
}
