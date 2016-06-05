package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.annotation.web.JdbcConn;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class ConnectionHandler extends BuildParamHandler.BuildParameter{
	public static final String NEED_JDBC_CONN ="need_jdbc_connection";
	public static final String COMMIT_JDBC_CONN ="commit_jdbc_connection";
	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		
		
		if((!mpe.getName().equals("con"))||(!"java.sql.Connection".equals(mpe.getTypeName())))
			throw new AptException(mpe.getRef(),"@JdbcConn java type must be java.sql.Connection ,parameter name must be con");
		rmcg.setAttribute(NEED_JDBC_CONN, Boolean.TRUE);
		rmcg.setAttribute(COMMIT_JDBC_CONN, mpe.getRef().getAnnotation(JdbcConn.class).value());
	}
	

}
