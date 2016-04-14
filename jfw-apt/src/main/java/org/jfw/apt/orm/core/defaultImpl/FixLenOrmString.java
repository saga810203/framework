package org.jfw.apt.orm.core.defaultImpl;

public class FixLenOrmString extends OrmString{
	@Override
	protected int getSqlType() {
		return java.sql.Types.CHAR;
	}

}
