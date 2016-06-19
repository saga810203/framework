package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.UpdateListOperateCG;

public @interface UpdateList {
	String value() default "PrimaryKey";
	String[] exincludeColumn() default {};
	Class<?> handlerClass() default UpdateListOperateCG.class;
}
