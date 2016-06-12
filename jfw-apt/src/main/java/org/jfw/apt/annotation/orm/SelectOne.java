package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.SelectOneOperateCG;

public @interface SelectOne {
	Class<?> handlerClass() default SelectOneOperateCG.class;
}
