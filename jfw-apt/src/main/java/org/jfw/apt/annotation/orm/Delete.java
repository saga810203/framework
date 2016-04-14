package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.orm.DeleteOperateCG;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Delete {
	String value() default "PrimaryKey";
	Class<?> target() default Object.class; 
	Class<?> handlerClass() default DeleteOperateCG.class;
}
