package org.jfw.apt.annotation.web.validparam;

import org.jfw.apt.model.web.handlers.validparam.MinValueHandler;

public @interface MinValue {
	Class<?> validParamClass() default MinValueHandler.class;
	String value() default "";
}
