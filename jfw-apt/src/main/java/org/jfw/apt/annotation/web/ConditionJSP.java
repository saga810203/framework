package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.annotation.Condition;
import org.jfw.apt.model.web.handlers.viewHandler.ConditionJspHandler;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ConditionJSP {
	Class<?> viewHandlerClass() default ConditionJspHandler.class;
	String prefix() default "";
	String defaultValue();
	String dataName() default "JFW_REQUEST_TO_JSP_DATA";
	Condition[] value();
	boolean enableJson() default false;
	int jsonViewType() default 1;
}
