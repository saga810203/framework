package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.web.handlers.BuildParamHandler.BuildParameter;
import org.jfw.apt.model.web.handlers.buildparam.RequestBodyHandler;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface RequestBody {
	String targetTypeName() default "";
	Class<? extends BuildParameter> buildParamClass() default RequestBodyHandler.class;
}
