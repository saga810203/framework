package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.orm.InsertOperateCG;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Insert {
	Class<?> handlerClass() default InsertOperateCG.class;
}
