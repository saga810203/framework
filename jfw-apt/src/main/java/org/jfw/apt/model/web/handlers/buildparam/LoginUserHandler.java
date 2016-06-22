package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.annotation.web.LoginUser;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class LoginUserHandler extends BuildParamHandler.BuildParameter {
	public static final String LOGIN_USER_FLAG_IN_SESSION ="_jfw_loginUser";
	
	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		rmcg.readSession(sb);
		LoginUser user = mpe.getRef().getAnnotation(LoginUser.class);
		if (user == null) 	return;
		
		sb.append(mpe.getTypeName()).append(" ").append(mpe.getName()).append(" = (").append(mpe.getTypeName())
					.append(")session.getAttribute(\"").append(LOGIN_USER_FLAG_IN_SESSION).append("\");");
		if(user.value() || (user.auth()>0)){
			sb.append("if(null == ").append(mpe.getName()).append(")throw new org.jfw.util.exception.JfwBaseException(1,\"no user login\");");
		}
		if(user.auth()>0){
			sb.append("if(!").append(mpe.getName()).append(".hasAuthority(").append(user.auth()).append(")throw new org.jfw.util.exception.JfwBaseException(2,\"Insufficient authority\");");
		}
	}
}
