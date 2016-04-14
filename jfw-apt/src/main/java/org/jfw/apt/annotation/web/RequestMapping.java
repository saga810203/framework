package org.jfw.apt.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.web.RequestMethod;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface RequestMapping {
	String value() default "";
	RequestMethod[] method() default {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE};
}
