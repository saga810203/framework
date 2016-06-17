package org.jfw.apt.orm.core.defaultImpl;

import java.util.Map;

import org.jfw.apt.model.AbstractMethodGenerater;
import org.jfw.apt.orm.core.OrmHandler;


public abstract class BaseOrmHandler implements OrmHandler {
	private static final String PARAM_INDEX = BaseOrmHandler.class
			.getName() + "_param_index";

	protected void checkParamIndex(StringBuilder sb, Map<String, Object> map) {
		if (map.get(PARAM_INDEX) == null) {
			sb.append("int _index = 1; \r\n");
			map.put(PARAM_INDEX, Boolean.TRUE);
		}
	}

	protected String getTempalteVariableName(Map<String, Object> map) {
		Object obj = map.get(AbstractMethodGenerater.TVN);
		int i = obj==null?0:((Integer)obj).intValue();		
		++i;
		map.put(AbstractMethodGenerater.TVN,i);
		return "_tmp"+i;	
	}
}
