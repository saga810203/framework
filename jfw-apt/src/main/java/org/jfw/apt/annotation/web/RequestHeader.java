package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.web.handlers.BuildParamHandler.BuildParameter;
import org.jfw.apt.model.web.handlers.buildparam.RequestHeaderHandler;
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface RequestHeader {
	String value() default "";
	Class<?> clazz() default Object.class;
	String defaultValue() default "";
	boolean required() default true;
	FieldParam[] fields() default {};
	String[] excludeFields() default {};
	Class<? extends BuildParameter> buildParamClass() default RequestHeaderHandler.class;
}
