package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.orm.CustomDMLSQLOperateCG;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface CustomDMLSQL {
	String value();
	SqlValue[] sqlValues() default {};
	Class<?> handlerClass() default CustomDMLSQLOperateCG.class;
}
