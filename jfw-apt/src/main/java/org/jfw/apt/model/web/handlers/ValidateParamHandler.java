package org.jfw.apt.model.web.handlers;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.jfw.apt.Utils;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.AbstractMethodGenerater;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestHandler;

public class ValidateParamHandler extends RequestHandler {

	@Override
	public void init() {

	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		List<MethodParamEntry> mpes = this.rmcg.getParams();

		for (int i = 0; i < mpes.size(); ++i) {
			MethodParamEntry mpe = mpes.get(i);
			List<? extends AnnotationMirror> ans = mpe.getRef().getAnnotationMirrors();
			for (AnnotationMirror anm : ans) {
				Object obj = Utils.getReturnValueOnAnnotation("validParamClass", anm);
				Class<ValidParameter> bpcls = Utils.getClass(obj, ValidParameter.class);
				if (bpcls != null) {
					ValidParameter vp;
					try {
						vp = bpcls.newInstance();
					} catch (Exception e) {
						throw new AptException(mpe.getRef(),
								"create Object instance with " + bpcls.getName() + "error:" + e.getMessage());
					}
					vp.build(sb, mpe, rmcg);
				}
			}
		}
	}
	public static class ValidParameter {		
		
		public void build(StringBuilder sb, MethodParamEntry mpe, AbstractMethodGenerater amg)
				throws AptException {
		}
	}
}
