package org.jfw.apt.orm.core.defaultImpl;

/**
 * 非空 int 
 * @author pengjia
 *
 */
public class UnOrmInt extends OrmInt {

	@Override
	public Class<?> supportsClass() {
		return int.class;
	}


}
