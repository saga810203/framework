package org.jfw.apt;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.orm.DBOperateCG;

public class DataBaseHandlerSupported extends AbstractCodeGenerateHandler {

	@Override
	public boolean isSupportedInterFace() {
		return false;
	}

	@Override
	public boolean isSupportedAbstractClass() {
		return true;
	}

	@Override
	public boolean isSupportedNoAbstractClass() {
		return false;
	}

	@Override
	protected void writeContent(StringBuilder sb) throws AptException {
		for (Element ele : this.ref.getEnclosedElements()) {
			if (ele.getKind() == ElementKind.METHOD) {
				List<? extends AnnotationMirror> ans = ele.getAnnotationMirrors();
				for (AnnotationMirror anm : ans) {
					Object obj = Utils.getReturnValueOnAnnotation("handlerClass", anm);
					Class<DBOperateCG> dbcgcls = Utils.getClass(obj, DBOperateCG.class);
					if (dbcgcls != null) {
						DBOperateCG dbocg = null;
						try {
							dbocg = dbcgcls.newInstance();
						} catch (Exception e) {
							throw new AptException(ele,
									"create Object instance with " + dbcgcls.getName() + "error:" + e.getMessage());
						}
						for (Map.Entry<String, Object> een : this.env.entrySet()) {
							dbocg.setAttribute(een.getKey(), een.getValue());
						}
						sb.append("\r\n").append(dbocg.getCode((ExecutableElement) ele)).append("\r\n");
						break;
					}

				}
			}
		}

	}

	@Override
	public boolean isManagedByBeanFactory() {
		return false;
	}

	@Override
	public boolean isGenerateClassManagedByBeanFactory() {
		return true;
	}

}
