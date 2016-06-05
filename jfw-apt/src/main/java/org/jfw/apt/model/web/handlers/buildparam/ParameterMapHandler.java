package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class ParameterMapHandler  extends BuildParamHandler.BuildParameter{

	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		sb.append("java.util.Map<String,String[]> ").append(mpe.getName()).append(" =  req.getParameterMap();\r\n");
	}

}
