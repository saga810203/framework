package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.web.handlers.BuildParamHandler.BuildParameter;
import org.jfw.apt.model.web.handlers.buildparam.PathVarHandler;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface PathVar {
	String value() default "";
	boolean encoding() default false;
	String pathAttribute() default "org.jfw.web.reqMacthUri_DYN";
	Class<? extends BuildParameter> buildParamClass() default PathVarHandler.class;
}
