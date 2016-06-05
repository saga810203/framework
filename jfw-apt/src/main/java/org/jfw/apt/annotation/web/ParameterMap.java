package org.jfw.apt.annotation.web;

import org.jfw.apt.model.web.handlers.BuildParamHandler.BuildParameter;
import org.jfw.apt.model.web.handlers.buildparam.ParameterMapHandler;

public @interface ParameterMap {
	Class<? extends BuildParameter> buildParamClass() default ParameterMapHandler.class;
}
