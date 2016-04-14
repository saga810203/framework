package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.WebHandlerSupported;
import org.jfw.apt.model.web.RequestHandler;
import org.jfw.apt.model.web.handlers.BuildParamHandler;
import org.jfw.apt.model.web.handlers.ExecuteHandler;
import org.jfw.apt.model.web.handlers.LastScriptHandler;
import org.jfw.apt.model.web.handlers.SetSessionHandler;
import org.jfw.apt.model.web.handlers.ViewHandler;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface WebHandler {
	Class<?> handlerClass() default WebHandlerSupported.class;
	Class<? extends RequestHandler>[] handler() default {ViewHandler.class, BuildParamHandler.class, ExecuteHandler.class,
			SetSessionHandler.class, LastScriptHandler.class };

	String value() default "";
	Class<?> defaultHandlerClass() default Object.class;
}
