package org.jfw.apt.annotation.web.validparam;

import org.jfw.apt.model.web.handlers.validparam.ValidScriptHandler;

public @interface ValidScript {
	Class<?> validParamClass() default ValidScriptHandler.class;
	String[] value() default {};
}
