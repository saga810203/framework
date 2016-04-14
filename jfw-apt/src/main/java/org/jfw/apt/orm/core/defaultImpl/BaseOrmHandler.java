package org.jfw.apt.orm.core.defaultImpl;

import java.util.Map;

import org.jfw.apt.orm.core.OrmHandler;


public abstract class BaseOrmHandler implements OrmHandler {
	private static final String PARAM_INDEX = BaseOrmHandler.class
			.getName() + "_param_index";
	private static final String TEMP_VAR_INDEX = BaseOrmHandler.class
			.getName() + "_temp_var_index";

	protected void checkParamIndex(StringBuilder sb, Map<String, Object> map) {
		if (map.get(PARAM_INDEX) == null) {
			sb.append("int paramIndex = 1; \r\n");
			map.put(PARAM_INDEX, Boolean.TRUE);
		}
	}

	protected String getTempalteVariableName(Map<String, Object> map) {
		int index = 0;
		Integer i = (Integer) map.get(TEMP_VAR_INDEX);
		if (i == null) {
			index = 0;
		} else {
			index = i.intValue();
		}
		++index;
		map.put(TEMP_VAR_INDEX, index);
		return "tmp_" + index;
	}
}
