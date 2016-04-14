package org.jfw.apt;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.jfw.apt.exception.AptException;

public interface CodeGenerateHandler {
	void setEnv(Map<String,Object> env);
	void handle(TypeElement ref,AnnotationMirror am,Object annotationObj) throws AptException;
	boolean isManagedByBeanFactory();
}
