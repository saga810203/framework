package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.orm.QueryOperateCG;
import org.jfw.apt.orm.core.enums.DataElement;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Query {
    String singleColumnSql() default "";
	boolean singleRow() default false;
	DataElement singleColumn() default DataElement.invalid_de;
	String otherSentence() default "";
	Where where() default @Where();
	Class<?> handlerClass() default QueryOperateCG.class;
}
