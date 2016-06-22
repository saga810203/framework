package org.jfw.apt.model.web.handlers;

import org.jfw.apt.annotation.web.ResultToNull;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.RequestHandler;

public class ResultToNullHandler extends RequestHandler {

	@Override
	public void init() throws AptException {
	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		if (null != this.getRmcg().getRef().getAnnotation(ResultToNull.class))
			sb.append("result = null;");
	}

}
