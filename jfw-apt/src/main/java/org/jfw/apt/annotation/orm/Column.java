package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.orm.core.enums.DataElement;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Column {
	DataElement value();
	boolean nullable() default true;
	boolean defaultQuery() default true;
	String comment() default "";
}
