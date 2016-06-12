package org.jfw.apt.model.web.handlers;

import java.util.List;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestHandler;

public class ExecuteHandler extends RequestHandler{

	@Override
	public void init() throws AptException {
		
	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		List<MethodParamEntry> mpes = this.rmcg.getParams();

		if(!this.rmcg.getWebHandlerSupported().isThreadSafe()){
			sb.append(this.rmcg.getWebHandlerSupported().getSourceClassname())
			.append(" handler  = (").append(this.rmcg.getWebHandlerSupported().getSourceClassname()).append(")handlerFactory.get();\r\n");
		}
		if(!"void".equals(this.rmcg.getReturnType())){
			sb.append("result = ");
		}
		sb.append(" handler.").append(this.getRmcg().getName()).append("(");
		if(mpes!=null&& mpes.size()>0){
			for(int i = 0 ; i < mpes.size() ; ++i){
				if(i!=0)sb.append(",");
				sb.append(mpes.get(i).getName());
			}
		}
		sb.append(");\r\n");
		
	}

}
