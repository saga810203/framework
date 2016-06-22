package org.jfw.apt.model.web.handlers;

import org.jfw.apt.annotation.web.ChangeResult;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.RequestHandler;

public class ChangeResultHandler extends RequestHandler{

	@Override
	public void init() throws AptException {
		
	}
	
	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		ChangeResult cr = this.getRmcg().getRef().getAnnotation(ChangeResult.class);
		if(cr!=null){
			String ss = cr.value();
			sb.append("result = ").append(this.getRmcg().getWebHandlerSupported().getSourceClassname()).append(".").append(ss).append("(result);");
		}
	}

}
