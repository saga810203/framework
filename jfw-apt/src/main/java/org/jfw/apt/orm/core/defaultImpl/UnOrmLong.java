package org.jfw.apt.orm.core.defaultImpl;

public class UnOrmLong extends OrmLong{

	@Override
	public Class<?> supportsClass() {
		return long.class;
	}

}
