package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.orm.core.enums.DE;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Column {
	DE value();
	boolean defaultQuery() default true;
	boolean insertable() default true;
	boolean renewable() default true;	
	
	String fixInsertSqlValue() default "";
	String fixUpdateSqlValue() default "";
	String dbType() default "";
	String comment() default "";
}
