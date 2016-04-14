package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.web.handlers.BuildParamHandler.BuildParameter;
import org.jfw.apt.model.web.handlers.buildparam.SessionValHandler;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface SessionVal {
	String value();
	boolean remove() default false;
	String defaultvalue() default "";
	Class<? extends BuildParameter> buildParamClass() default SessionValHandler.class;
}
