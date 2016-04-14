package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.web.SessionVal;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class SessionValHandler extends BuildParamHandler.BuildParameter {

	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		rmcg.readSession(sb);
		SessionVal sv = mpe.getRef().getAnnotation(SessionVal.class);
		if (sv == null)
			return;
		String val = sv.value();
		String dv = Utils.emptyToNull(sv.defaultvalue());
		if (Utils.isPrimitive(mpe.getTypeName())) {
			String wn = Utils.getWrapClass(mpe.getTypeName());
			sb.append(mpe.getTypeName()).append(" ").append(mpe.getName()).append(";");
			String ln = rmcg.getTempalteVariableName();
			sb.append(wn).append(" ").append(ln).append(" = (").append(wn).append(")session.getAttribute(\"")
					.append(val).append("\");");
			sb.append("if(null!=").append(ln).append("){").append(mpe.getName()).append(" = ").append(ln).append(";}");
			if (dv != null) {
				sb.append("else{").append(mpe.getName()).append("=").append(dv).append(";}");
			}
		} else {
			sb.append(mpe.getTypeName()).append(" ").append(mpe.getName()).append(" = (").append(mpe.getTypeName())
					.append(")session.getAttribute(\"").append(val).append("\");");
			if (dv != null) {
				sb.append("if(null==").append(mpe.getName()).append("){").append(mpe.getName()).append(" = ").append(dv)
						.append(";}");
			}
		}
		if (sv.remove()) {
			sb.append("session.removeAttribute(\"").append(val).append("\");");
		}

	}

}
