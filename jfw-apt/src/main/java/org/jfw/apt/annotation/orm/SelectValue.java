package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.SelectValueOperateCG;
import org.jfw.apt.orm.core.enums.DE;

public @interface SelectValue {
	String sql();
	DE de();
	Class<?> handlerClass() default SelectValueOperateCG.class;
}
