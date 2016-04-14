package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.orm.UpdateOperateCG;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Update {
	boolean dynamicValue() default false;
	String value() default "PrimaryKey";
	Class<?> handlerClass() default UpdateOperateCG.class;
}
