package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.WebHandlerSupported;
import org.jfw.apt.model.web.RequestHandler;
import org.jfw.apt.model.web.handlers.BuildParamHandler;
import org.jfw.apt.model.web.handlers.ChangeResultHandler;
import org.jfw.apt.model.web.handlers.ExecuteHandler;
import org.jfw.apt.model.web.handlers.LastScriptHandler;
import org.jfw.apt.model.web.handlers.LoginUserHandler;
import org.jfw.apt.model.web.handlers.SetSessionHandler;
import org.jfw.apt.model.web.handlers.ValidateParamHandler;
import org.jfw.apt.model.web.handlers.ViewHandler;
import org.jfw.apt.model.web.handlers.JdbcConnectionHandler;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface WebHandler {
	Class<?> handlerClass() default WebHandlerSupported.class;

	Class<? extends RequestHandler>[] handler() default { ViewHandler.class,LoginUserHandler.class, BuildParamHandler.class,ValidateParamHandler.class,
			JdbcConnectionHandler.class, ExecuteHandler.class, SetSessionHandler.class,ChangeResultHandler.class,LastScriptHandler.class };
	String value() default "";
//	Class<?> defaultHandlerClass() default Object.class;
}
