package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.UpdateWithOperateCG;

public @interface UpdateWith {
	/**
	 * where field name
	 * @return
	 */
	String[] where();
	/**
	 * target is a persistentObject(Kind = TABLE)
	 * @return
	 */
	Class<?> target(); 
	String[] includeFixValueColumn() default {};
	String value() default "";
	Class<?> handlerClass() default UpdateWithOperateCG.class;
}
