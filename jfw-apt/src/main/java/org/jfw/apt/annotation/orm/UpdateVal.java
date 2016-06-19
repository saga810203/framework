package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.UpdateValOperateCG;

public @interface UpdateVal {
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
	boolean includeFixValues() default true;
	Class<?> handlerClass() default UpdateValOperateCG.class;
}
