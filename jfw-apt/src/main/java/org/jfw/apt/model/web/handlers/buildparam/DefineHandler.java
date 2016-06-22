package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.Utils;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class DefineHandler extends BuildParamHandler.BuildParameter{
	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		
		String tn = mpe.getTypeName();
		sb.append(tn).append(" ").append(mpe.getName());
		if(!Utils.isPrimitive(tn)) sb.append(" = null");
		sb.append(";");
	}
	

}
