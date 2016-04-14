package org.jfw.apt.model.web.handlers;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.jfw.apt.Utils;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestHandler;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.buildparam.RequestParamHandler;

public class BuildParamHandler extends RequestHandler {

	@Override
	public void init() {

	}

	private BuildParameter getBuilderParameter(MethodParamEntry mpe) throws AptException {
		List<? extends AnnotationMirror> ans = mpe.getRef().getAnnotationMirrors();
		for (AnnotationMirror anm : ans) {
			Object obj = Utils.getReturnValueOnAnnotation("buildParamClass", anm);
			Class<BuildParameter> bpcls = Utils.getClass(obj,BuildParameter.class);
			if(bpcls!=null){
				try {
					return  bpcls.newInstance();
				} catch (Exception e) {
					throw new AptException(mpe.getRef(),
							"create Object instance with " + bpcls.getName() + "error:" + e.getMessage());
				}
			}

		}
		return null;
	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		List<MethodParamEntry> mpes = this.rmcg.getParams();

		for (int i = 0; i < mpes.size(); ++i) {
			MethodParamEntry mpe = mpes.get(i);
			BuildParameter bp = this.getBuilderParameter(mpe);
			if (bp == null)
				bp = new RequestParamHandler();
			bp.build(sb, mpe, this.getRmcg());
		}

	}

	public static class BuildParameter {
		public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg)
				throws AptException {
		}
	}
}
