package org.jfw.apt.model.web.handlers;

import java.util.List;

import org.jfw.apt.annotation.web.LoginUser;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestHandler;

public class LoginUserHandler extends RequestHandler{
	
	public static final String LOGIN_USER_FLAG_IN_SESSION ="_jfw_loginUser";

	@Override
	public void init() throws AptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		LoginUser user = this.getRmcg().getRef().getAnnotation(LoginUser.class);
		
		String pn = null;
		String tn = "org.jfw.util.auth.AuthUser";
		if(user==null){
			 List<MethodParamEntry> mpes =this.getRmcg().getParams();
					 
			 for(MethodParamEntry mpe:mpes){
				 user = mpe.getRef().getAnnotation(LoginUser.class);
				 if(null!=user){
					pn = mpe.getName();
					tn = mpe.getTypeName();
					break;
				 }
			 }
			
		}else{
			pn = this.getRmcg().getTempalteVariableName();
		}
		if(null == user) return;
		rmcg.readSession(sb);
		sb.append(tn).append(" ").append(pn).append(" = (").append(tn)
					.append(")session.getAttribute(\"").append(LOGIN_USER_FLAG_IN_SESSION).append("\");");
		if(user.value() || (user.auth()>0)){
			sb.append("if(null == ").append(pn).append(")throw new org.jfw.util.exception.JfwBaseException(1,\"no user login\");");
		}
		if(user.auth()>0){
			sb.append("if(!").append(pn).append(".hasAuthority(").append(user.auth()).append(")throw new org.jfw.util.exception.JfwBaseException(100,\"Insufficient authority\");");
		}
	}
}
