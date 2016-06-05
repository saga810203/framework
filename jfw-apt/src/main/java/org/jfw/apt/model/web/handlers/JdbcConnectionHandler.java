package org.jfw.apt.model.web.handlers;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.RequestHandler;
import org.jfw.apt.model.web.handlers.buildparam.ConnectionHandler;

public class JdbcConnectionHandler  extends RequestHandler{
	
	private boolean use = false;
	private boolean commit = false;

	@Override
	public void init() throws AptException {
		
	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		this.use = Boolean.TRUE.equals(this.getRmcg().getAttribute(ConnectionHandler.NEED_JDBC_CONN));
		this.commit = Boolean.TRUE.equals(this.getRmcg().getAttribute(ConnectionHandler.COMMIT_JDBC_CONN));
		if(this.use){
			sb.append("java.sql.Connection con = this.dataSource.getConnection();\r\n");
			sb.append("try{");			
		}
	}

	@Override
	public void appendAfterCode(StringBuilder sb) throws AptException {
		if(this.use){
			if(this.commit){
				sb.append("con.commit();");
				String tmp = this.getRmcg().getTempalteVariableName();
				String tmp1 = this.getRmcg().getTempalteVariableName();
				sb.append("}catch(Throwable ").append(tmp)
				.append("){try{con.rollback();}catch(Throwable ").append(tmp1).append("){}\r\n")
				.append("throw ").append(tmp).append(";");
			}
			String tmp2 = this.getRmcg().getTempalteVariableName();
			sb.append("}finally{try{con.close();}catch(Throwable ").append(tmp2).append("){}\r\n}");
		}
	}
}
