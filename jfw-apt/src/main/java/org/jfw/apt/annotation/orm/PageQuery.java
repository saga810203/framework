package org.jfw.apt.annotation.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jfw.apt.model.orm.PageQueryOperateCG;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PageQuery {
	String otherSentence() default "";
	Where where() default @Where();
	Class<?> handlerClass() default PageQueryOperateCG.class;
}
