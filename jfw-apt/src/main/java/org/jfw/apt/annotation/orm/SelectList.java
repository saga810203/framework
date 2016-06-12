package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.SelectListOperateCG;

public @interface SelectList {
	String value() default "";
	Class<?> handlerClass() default SelectListOperateCG.class;
}
