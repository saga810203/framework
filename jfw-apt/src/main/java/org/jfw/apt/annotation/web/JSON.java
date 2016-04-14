package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.web.handlers.viewHandler.JsonHandler;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JSON {
	Class<?> viewHandlerClass() default JsonHandler.class;
}
