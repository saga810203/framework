package org.jfw.apt.orm.core.defaultImpl;

public class OrmShort extends AbstractOrmHandler{

	@Override
	public Class<?> supportsClass() {
		return Short.class;
	}

	@Override
	public String getReadMethod() {
		return "getShort";
	}

	@Override
	public String getWriteMethod() {
		return "setShort";
	}

	@Override
	protected int getSqlType() {
		return java.sql.Types.SMALLINT;
	}

}
