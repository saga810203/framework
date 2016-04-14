package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.web.handlers.viewHandler.JspHandler;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JSP {
	Class<?> viewHandlerClass() default JspHandler.class;
	String prefix() default "";
	String value();
	String dataName() default "JFW_REQUEST_TO_JSP_DATA";
	boolean enableJson() default false;
	int jsonViewType() default 1;
}
