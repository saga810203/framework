package org.jfw.apt.model.web.handlers;

import java.util.List;

import org.jfw.apt.annotation.web.JdbcConn;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestHandler;

public class JdbcConnectionHandler extends RequestHandler {
	private boolean _commit = false;
	private String _pn = null;

	@Override
	public void init() throws AptException {

	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		List<MethodParamEntry> mpes = this.getRmcg().getParams();
		_commit = false;
		_pn = null;

		for (MethodParamEntry mpe : mpes) {
			JdbcConn con = mpe.getRef().getAnnotation(JdbcConn.class);
			if (null != con) {
				if (!"java.sql.Connection".equals(mpe.getTypeName()))
					throw new AptException(mpe.getRef(), "@JdbcConn java type must be java.sql.Connection");
				_pn = mpe.getName();
				_commit = con.value();
				break;
			}
		}
		if (_pn != null) {
			sb.append(_pn).append("= this.dataSource.getConnection();\r\n");
			sb.append("try{");
		}
	}

	@Override
	public void appendAfterCode(StringBuilder sb) throws AptException {
		if (this._pn != null) {
			if (this._commit) {
				sb.append(this._pn).append(".commit();");
				String tmp = this.getRmcg().getTempalteVariableName();
				String tmp1 = this.getRmcg().getTempalteVariableName();
				sb.append("}catch(Throwable ").append(tmp).append("){try{").append(_pn)
						.append(".rollback();}catch(Throwable ").append(tmp1).append("){}\r\n").append("throw ")
						.append(tmp).append(";");
			}
			String tmp2 = this.getRmcg().getTempalteVariableName();
			sb.append("}finally{try{").append(_pn).append(".close();}catch(Throwable ").append(tmp2).append("){}\r\n}");
		}
	}
}
