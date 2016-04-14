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
//		String ln = this.getRmcg().getTempalteVariableName();
		
		List<MethodParamEntry> mpes = this.rmcg.getParams();
//		if(!this.rmcg.getWebHandlerSupported().isThreadSafe()){
//			sb.append(this.rmcg.getWebHandlerSupported().getPackageName()).append(this.rmcg.getWebHandlerSupported().getClassName())
//			.append(" handler = null;\r\ntry{handler = (").append(this.rmcg.getWebHandlerSupported().getPackageName())
//			.append(this.rmcg.getWebHandlerSupported().getClassName()).append(")")
//			.append(this.getRmcg().getWebHandlerSupported().getTargetClassName())
//			.append(".handlerClass.newInstance();\r\n}catch(Exception ").append(ln).append("){throw new RuntimeException(\"create object instance error with class name:\"+")
//			.append(this.getRmcg().getWebHandlerSupported().getTargetClassName()).append(".handlerClass.getName());}");
//		}
		if(!this.rmcg.getWebHandlerSupported().isThreadSafe()){
			sb.append(this.rmcg.getWebHandlerSupported().getPackageName()).append(this.rmcg.getWebHandlerSupported().getClassName())
			.append(" handler  = (").append(this.rmcg.getWebHandlerSupported().getPackageName())
			.append(this.rmcg.getWebHandlerSupported().getClassName()).append(")handlerFactory.get();\r\n");
		}
		
		
		if(!"void".equals(this.rmcg.getReturnType())){
			sb.append("result = ");
		}
//		if(this.rmcg.getWebHandlerSupported().isThreadSafe()){
//			sb.append(this.rmcg.getWebHandlerSupported().getTargetClassName()).append(".");
//		}
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
