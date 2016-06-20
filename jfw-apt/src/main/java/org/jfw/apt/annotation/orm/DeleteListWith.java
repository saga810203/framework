package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.orm.DeleteListWithOperateCG;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface DeleteListWith {
	String value() default "";
	Class<?> target(); 
	Class<?> handlerClass() default DeleteListWithOperateCG.class;
}
