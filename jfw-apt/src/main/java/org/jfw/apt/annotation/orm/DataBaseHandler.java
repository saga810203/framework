package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.DataBaseHandlerSupported;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface DataBaseHandler {
	Class<?> handlerClass() default DataBaseHandlerSupported.class;
}
