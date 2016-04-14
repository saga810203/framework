package org.jfw.apt.orm.core.defaultImpl;

public class OrmBigDecimal extends AbstractOrmHandler{

	@Override
	public Class<?> supportsClass() {
		return java.math.BigDecimal.class;
	}

	@Override
	public String getReadMethod() {
		return "getBigDecimal";
	}

	@Override
	public String getWriteMethod() {
		return "setBigDecimal";
	}

	@Override
	protected int getSqlType() {
		return java.sql.Types.DECIMAL;
	}
}
